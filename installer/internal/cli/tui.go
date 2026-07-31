package cli

import (
	"fmt"
	"strings"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
	"github.com/devopsnotes/k-guard-ai/installer/internal/kube"
)

type mode int

const (
	modeMenu mode = iota
	modeInstallCheck
	modeInstallNamespaceInput
	modeInstallCredentialsInput
	modeInstallSummary
	modeInstallApplying
	modeInstallDone
	modeCheckCluster
)

type menuItem int

const (
	menuCheck menuItem = iota
	menuInstall
	menuStatus
	menuQuit
)

// installStepMsg is used to send messages from async installation/check steps
// back to the TUI model.
type installStepMsg struct {
	err error
	msg string
}

// model holds the full state of the TUI.
type model struct {
	mode   mode
	cursor int

	// menu status
	status string

	// install state
	namespace    string
	esUsername   string
	esPassword   string
	installLog   []string
	installError error

	// input buffer for namespace / credentials
	inputLabel string
	inputValue string
	inputField string // "namespace", "username", "password"
}

var (
	titleStyle = lipgloss.NewStyle().
			Bold(true).
			Foreground(lipgloss.Color("#00B5AD")).
			Padding(1, 2)

	itemStyle = lipgloss.NewStyle().
			PaddingLeft(2)

	selectedStyle = itemStyle.Copy().
			Foreground(lipgloss.Color("#FFFFFF")).
			Background(lipgloss.Color("#00B5AD"))

	statusStyle = lipgloss.NewStyle().
			MarginTop(1).
			Foreground(lipgloss.Color("#AAAAAA"))

	borderStyle = lipgloss.NewStyle().
			Border(lipgloss.RoundedBorder()).
			BorderForeground(lipgloss.Color("#00B5AD")).
			Padding(1, 2).
			Margin(1, 2)
)

// initialModel instances a new model with default values.
func initialModel() model {
	return model{
		mode:   modeMenu,
		cursor: 0,
		status: "",
	}
}

// Init implements tea.Model.Init.
func (m model) Init() tea.Cmd {
	return nil
}

// checkClusterCmd runs kubectl + cluster access checks (async).
func checkClusterCmd() tea.Cmd {
	return func() tea.Msg {
		if err := kube.CheckKubectl(); err != nil {
			return installStepMsg{err: err, msg: "[check] kubectl check failed"}
		}
		if err := kube.RequireClusterAccess(); err != nil {
			return installStepMsg{err: err, msg: "[check] cluster access failed"}
		}
		return installStepMsg{err: nil, msg: "[check] Cluster and kubectl OK"}
	}
}

// applyResourcesCmd applies K-Guard resources (async).
func applyResourcesCmd(namespace, esUser, esPass string) tea.Cmd {
	return func() tea.Msg {
		logs := []string{}

		logs = append(logs, fmt.Sprintf("[install] Target namespace: %s", namespace))
		if !kube.NamespaceExists(namespace) {
			logs = append(logs, fmt.Sprintf("[install] Creating namespace: %s", namespace))
			if err := kube.EnsureNamespace(namespace); err != nil {
				return installStepMsg{
					err: err,
					msg: strings.Join(append(logs, "[install] Failed to create namespace"), "\n"),
				}
			}
		}

		logs = append(logs, "[install] Applying Elasticsearch Secret...")
		if err := kube.ApplyElasticsearchSecret(namespace, "kguard-ai-secret", esUser, esPass); err != nil {
			return installStepMsg{
				err: err,
				msg: strings.Join(append(logs, "[install] Failed to apply secret"), "\n"),
			}
		}

		logs = append(logs, "[install] Applying ConfigMap...")
		if err := kube.ApplyFile(namespace, "../k8s/configmap.yaml"); err != nil {
			return installStepMsg{
				err: err,
				msg: strings.Join(append(logs, "[install] Failed to apply configmap"), "\n"),
			}
		}

		logs = append(logs, "[install] Applying Deployment...")
		if err := kube.ApplyFile(namespace, "../k8s/deployment.yaml"); err != nil {
			return installStepMsg{
				err: err,
				msg: strings.Join(append(logs, "[install] Failed to apply deployment"), "\n"),
			}
		}

		logs = append(logs, "[install] Applying Service...")
		if err := kube.ApplyFile(namespace, "../k8s/service.yaml"); err != nil {
			return installStepMsg{
				err: err,
				msg: strings.Join(append(logs, "[install] Failed to apply service"), "\n"),
			}
		}

		logs = append(logs, "[install] Waiting for rollout...")
		if err := kube.RolloutStatus(namespace, "kguard-ai"); err != nil {
			return installStepMsg{
				err: err,
				msg: strings.Join(append(logs, "[install] Rollout failed"), "\n"),
			}
		}

		logs = append(logs, "K-Guard AI installation completed")
		return installStepMsg{
			err: nil,
			msg: strings.Join(logs, "\n"),
		}
	}
}

// Update implements tea.Model.Update.
func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {

	case tea.KeyMsg:
		switch m.mode {

		// --- MAIN MENU ---
		case modeMenu:
			switch msg.String() {
			case "ctrl+c", "q":
				return m, tea.Quit

			case "up", "k":
				if m.cursor > 0 {
					m.cursor--
				}
			case "down", "j":
				if m.cursor < 3 {
					m.cursor++
				}
			case "enter":
				switch menuItem(m.cursor) {
				case menuCheck:
					// TUI check (no direct stdout)
					m.mode = modeCheckCluster
					m.installLog = []string{"[check] Checking kubectl and cluster access..."}
					m.installError = nil
					return m, checkClusterCmd()

				case menuStatus:
					if err := runStatus(); err != nil {
						m.status = fmt.Sprintf("❌ status failed: %v", err)
					} else {
						m.status = "✅ status fetched successfully"
					}

				case menuInstall:
					// Start wizard: check, then namespace, then creds, then summary, then apply.
					m.mode = modeInstallCheck
					m.installLog = []string{"[install] Checking kubectl and cluster access..."}
					m.installError = nil
					return m, checkClusterCmd()

				case menuQuit:
					return m, tea.Quit
				}
			}

		// --- CHECK CLUSTER TUI ---
		case modeCheckCluster:
			switch msg.String() {
			case "ctrl+c":
				return m, tea.Quit
			case "enter", "q":
				// back to menu
				m.mode = modeMenu
				m.installLog = nil
				m.installError = nil
				m.status = "Check finished"
				return m, nil
			}

		// --- INPUT NAMESPACE ---
		case modeInstallNamespaceInput:
			switch msg.String() {
			case "ctrl+c":
				return m, tea.Quit

			case "enter":
				value := strings.TrimSpace(m.inputValue)
				if value == "" {
					m.status = "namespace cannot be empty"
					return m, nil
				}
				m.namespace = value
				// next: credentials
				m.mode = modeInstallCredentialsInput
				m.inputLabel = "Elasticsearch username"
				m.inputField = "username"
				m.inputValue = ""
				return m, nil

			case "backspace":
				if len(m.inputValue) > 0 {
					m.inputValue = m.inputValue[:len(m.inputValue)-1]
				}

			default:
				if len(msg.String()) == 1 {
					m.inputValue += msg.String()
				}
			}

		// --- INPUT CREDENTIALS ---
		case modeInstallCredentialsInput:
			switch msg.String() {
			case "ctrl+c":
				return m, tea.Quit

			case "enter":
				value := strings.TrimSpace(m.inputValue)
				if value == "" {
					m.status = "value cannot be empty"
					return m, nil
				}
				if m.inputField == "username" {
					m.esUsername = value
					m.inputLabel = "Elasticsearch password"
					m.inputField = "password"
					m.inputValue = ""
					return m, nil
				}
				if m.inputField == "password" {
					m.esPassword = value
					// next: summary
					m.mode = modeInstallSummary
					return m, nil
				}

			case "backspace":
				if len(m.inputValue) > 0 {
					m.inputValue = m.inputValue[:len(m.inputValue)-1]
				}

			default:
				if len(msg.String()) == 1 {
					m.inputValue += msg.String()
				}
			}

		// --- SUMMARY & CONFIRMATION ---
		case modeInstallSummary:
			switch msg.String() {
			case "ctrl+c":
				return m, tea.Quit

			case "y":
				m.installLog = append(m.installLog, "[install] Proceeding with installation...")
				m.mode = modeInstallApplying
				return m, applyResourcesCmd(m.namespace, m.esUsername, m.esPassword)

			case "n", "q":
				m.installLog = append(m.installLog, "[install] Installation cancelled by user")
				m.mode = modeInstallDone
				return m, nil
			}

		// --- END OF THE WIZARD ---
		case modeInstallDone:
			switch msg.String() {
			case "enter", "q":
				// back to menu
				m.mode = modeMenu
				m.status = "Install finished"
				m.installLog = nil
				m.installError = nil
				m.inputValue = ""
				m.inputLabel = ""
				m.inputField = ""
				return m, nil

			case "ctrl+c":
				return m, tea.Quit
			}
		}

	// --- ASYNC MESSAGE (check + install) ---
	case installStepMsg:
		m.installLog = append(m.installLog, msg.msg)
		m.installError = msg.err

		// Wizard install: progress
		if m.mode == modeInstallCheck && msg.err == nil {
			m.mode = modeInstallNamespaceInput
			m.inputLabel = "Target namespace for K-Guard AI"
			m.inputField = "namespace"
			m.inputValue = ""
			return m, nil
		}

		if m.mode == modeInstallApplying {
			m.mode = modeInstallDone
			return m, nil
		}

		// Check cluster TUI: just show result, user returns to menu manually
		if m.mode == modeCheckCluster {
			return m, nil
		}

		if msg.err != nil {
			m.mode = modeInstallDone
		}
		return m, nil
	}

	return m, nil
}

// View implements tea.Model.View.
func (m model) View() string {
	switch m.mode {
	case modeMenu:
		return m.viewMenu()
	case modeInstallCheck,
		modeInstallNamespaceInput,
		modeInstallCredentialsInput,
		modeInstallSummary,
		modeInstallApplying,
		modeInstallDone,
		modeCheckCluster:
		return m.viewInstall()
	default:
		return m.viewMenu()
	}
}

// viewMenu displays the main menu view.
func (m model) viewMenu() string {
	items := []string{
		"Check cluster & kubectl",
		"Install K-Guard AI (TUI wizard)",
		"Show K-Guard AI status",
		"Quit",
	}

	var content string
	content += titleStyle.Render("K-Guard AI Installer") + "\n\n"

	for i, label := range items {
		cursor := " "
		style := itemStyle
		if m.cursor == i {
			cursor = ">"
			style = selectedStyle
		}
		content += style.Render(fmt.Sprintf("%s %s", cursor, label)) + "\n"
	}

	if m.status != "" {
		content += "\n" + statusStyle.Render(m.status)
	}

	content = borderStyle.Render(content)

	footer := lipgloss.NewStyle().
		Foreground(lipgloss.Color("#888888")).
		Render("↑/↓ or j/k to move, enter to select, q to quit")

	return content + "\n\n" + footer
}

// viewInstall shows the installation/check wizard view.
func (m model) viewInstall() string {
	var b strings.Builder

	// Title with some top margin
	b.WriteString(titleStyle.Render("K-Guard AI Installer - TUI install wizard"))
	b.WriteString("\n\n\n")

	// Step block
	var step strings.Builder

	switch m.mode {
	case modeCheckCluster:
		step.WriteString("Cluster & kubectl check\n\n")
		if m.installError != nil {
			step.WriteString(fmt.Sprintf("❌ Check failed: %v\n\n", m.installError))
		} else {
			step.WriteString("✅ Check completed successfully\n\n")
		}
		step.WriteString("Press Enter or q to return to menu.\n")

	case modeInstallCheck:
		step.WriteString("Step 1/6: Checking kubectl and cluster access...\n\n")
		step.WriteString("Please wait.\n")

	case modeInstallNamespaceInput:
		step.WriteString("Step 2/6: Target namespace\n\n")
		step.WriteString(fmt.Sprintf("%s: %s\n\n", m.inputLabel, m.inputValue))
		step.WriteString("Press Enter to validate.\n")

	case modeInstallCredentialsInput:
		step.WriteString("Step 3/6: Elasticsearch credentials\n\n")
		display := m.inputValue
		if m.inputField == "password" {
			display = strings.Repeat("*", len(m.inputValue))
		}
		step.WriteString(fmt.Sprintf("%s: %s\n\n", m.inputLabel, display))
		step.WriteString("Press Enter to validate.\n")

	case modeInstallSummary:
		step.WriteString("Step 4/6: Summary & confirmation\n\n")
		step.WriteString(fmt.Sprintf("Namespace : %s\n", m.namespace))
		step.WriteString("Secret    : kguard-ai-secret (will be created or updated)\n")
		step.WriteString("ConfigMap : kguard-ai-config\n")
		step.WriteString("Deployment: kguard-ai\n")
		step.WriteString("Service   : kguard-ai\n\n")
		step.WriteString("Proceed with installation? [y/N]\n")

	case modeInstallApplying:
		step.WriteString("Step 5/6: Applying manifests & waiting for rollout...\n\n")
		step.WriteString("Installation in progress.\n")

	case modeInstallDone:
		step.WriteString("Step 6/6: Done\n\n")
		if m.installError != nil {
			step.WriteString(fmt.Sprintf("❌ Installation finished with error: %v\n\n", m.installError))
		} else {
			step.WriteString("✅ Installation completed successfully\n\n")
		}
		step.WriteString("Press Enter or q to return to menu.\n")
	}

	// Frame around the step
	b.WriteString(borderStyle.Render(strings.TrimRight(step.String(), "\n")))
	b.WriteString("\n\n\n")

	// Logs block
	if len(m.installLog) > 0 {
		b.WriteString("Logs:\n\n")
		for _, line := range m.installLog {
			b.WriteString("  ")
			b.WriteString(line)
			b.WriteString("\n")
		}
		b.WriteString("\n\n")
	}

	// Status line
	if m.status != "" {
		b.WriteString(m.status)
		b.WriteString("\n\n")
	}

	// Footer
	b.WriteString("ctrl+c to quit, use Enter / y / n depending on the step")

	return b.String()
}

// RunTUI starts the TUI installer.
func RunTUI() error {
	p := tea.NewProgram(
		initialModel(),
		tea.WithAltScreen(),
	)
	if _, err := p.Run(); err != nil {
		return fmt.Errorf("tui error: %w", err)
	}
	return nil
}
