package cli

import (
    "fmt"

    tea "github.com/charmbracelet/bubbletea"
    "github.com/charmbracelet/lipgloss"
)

type menuItem int

const (
    menuCheck menuItem = iota
    menuInstall
    menuStatus
    menuQuit
)

type model struct {
    cursor int
    status string
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

func initialModel() model {
    return model{
        cursor: 0,
        status: "",
    }
}

func (m model) Init() tea.Cmd {
    return nil
}

func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
    switch msg := msg.(type) {
    case tea.KeyMsg:
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
                if err := runCheck(); err != nil {
                    m.status = fmt.Sprintf("❌ check failed: %v", err)
                } else {
                    m.status = "✅ check completed successfully"
                }
            case menuInstall:
                if err := runInstall(); err != nil {
                    m.status = fmt.Sprintf("❌ install failed: %v", err)
                } else {
                    m.status = "✅ installation completed successfully"
                }
            case menuStatus:
                if err := runStatus(); err != nil {
                    m.status = fmt.Sprintf("❌ status failed: %v", err)
                } else {
                    m.status = "✅ status fetched successfully"
                }
            case menuQuit:
                return m, tea.Quit
            }
        }
    }
    return m, nil
}

func (m model) View() string {
    items := []string{
        "Check cluster & kubectl",
        "Install K-Guard AI",
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

// RunTUI démarre l’interface graphique terminal.
func RunTUI() error {
    p := tea.NewProgram(initialModel())
    if _, err := p.Run(); err != nil {
        return fmt.Errorf("tui error: %w", err)
    }
    return nil
}
