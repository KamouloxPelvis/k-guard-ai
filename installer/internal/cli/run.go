package cli

import (
	"bufio"
	"fmt"
	"os"
	"strings"

	"github.com/devopsnotes/k-guard-ai/installer/internal/kube"
	"golang.org/x/term"
)

func Run(args []string) error {
	if len(args) == 0 {
		printUsage()
		return nil
	}

	switch args[0] {
	case "check":
		return runCheck()
	case "install":
		return runInstall()
	case "status":
		return runStatus()
	default:
		return fmt.Errorf("unknown command: %s", args[0])
	}
}

func printUsage() {
	fmt.Println("Usage:")
	fmt.Println("  kguard-ai-installer check")
	fmt.Println("  kguard-ai-installer install")
	fmt.Println("  kguard-ai-installer status")
}

func runCheck() error {
	fmt.Println("[1/3] Checking kubectl...")
	if err := kube.CheckKubectl(); err != nil {
		return err
	}
	fmt.Println("kubectl: OK")

	fmt.Println("[2/3] Checking cluster access...")
	if kube.HasClusterAccess() {
		fmt.Println("cluster access: OK")
	} else {
		fmt.Println("cluster access: unavailable in current environment")
		fmt.Println("hint: provide a valid kubeconfig only when testing installation against a real K3s/Kubernetes cluster")
		return nil
	}

	fmt.Println("[3/3] Detecting preferred namespace...")
	preferred := kube.DetectPreferredNamespace()
	if preferred != "" {
		fmt.Printf("preferred namespace: %s\n", preferred)
	} else {
		fmt.Println("preferred namespace: none detected")
	}

	return nil
}

func runInstall() error {
	if err := kube.CheckKubectl(); err != nil {
		return err
	}
	if err := kube.RequireClusterAccess(); err != nil {
		return err
	}

	namespace, err := resolveNamespace()
	if err != nil {
		return err
	}

	fmt.Printf("[install] Target namespace: %s\n", namespace)

	if !confirmInstall(namespace) {
		return fmt.Errorf("installation cancelled by user")
	}

	if !kube.NamespaceExists(namespace) {
		fmt.Printf("[install] Creating namespace: %s\n", namespace)
		if err := kube.EnsureNamespace(namespace); err != nil {
			return err
		}
	}

	esUsername, esPassword, err := promptElasticsearchCredentials()
	if err != nil {
		return err
	}

	fmt.Println("[install] Applying Elasticsearch Secret...")
	if err := kube.ApplyElasticsearchSecret(namespace, "kguard-ai-secret", esUsername, esPassword); err != nil {
		return err
	}

	fmt.Println("[install] Applying ConfigMap...")
	if err := kube.ApplyFile(namespace, "../k8s/configmap.yaml"); err != nil {
		return err
	}

	fmt.Println("[install] Applying Deployment...")
	if err := kube.ApplyFile(namespace, "../k8s/deployment.yaml"); err != nil {
		return err
	}

	fmt.Println("[install] Applying Service...")
	if err := kube.ApplyFile(namespace, "../k8s/service.yaml"); err != nil {
		return err
	}

	fmt.Println("[install] Waiting for rollout...")
	if err := kube.RolloutStatus(namespace, "kguard-ai"); err != nil {
		return err
	}

	fmt.Println("K-Guard AI installation completed")
	return nil
}

func runStatus() error {
	if err := kube.CheckKubectl(); err != nil {
		return err
	}
	if err := kube.RequireClusterAccess(); err != nil {
		return err
	}

	namespace, err := resolveNamespace()
	if err != nil {
		return err
	}
	return kube.GetStatus(namespace)
}

func resolveNamespace() (string, error) {
	preferred := kube.DetectPreferredNamespace()
	if preferred != "" {
		fmt.Printf("Detected existing K-Guard namespace: %s\n", preferred)
		return preferred, nil
	}

	fmt.Print("Enter target namespace for K-Guard AI: ")
	reader := bufio.NewReader(os.Stdin)
	ns, err := reader.ReadString('\n')
	if err != nil {
		return "", err
	}

	ns = strings.TrimSpace(ns)
	if ns == "" {
		return "", fmt.Errorf("namespace cannot be empty")
	}

	return ns, nil
}

func confirmInstall(namespace string) bool {
	reader := bufio.NewReader(os.Stdin)

	fmt.Println("[install] Summary")
	fmt.Printf("  Namespace : %s\n", namespace)
	fmt.Println("  Secret    : kguard-ai-secret (will be created or updated)")
	fmt.Println("  ConfigMap : kguard-ai-config")
	fmt.Println("  Deployment: kguard-ai")
	fmt.Println("  Service   : kguard-ai")
	fmt.Print("Proceed? [y/N]: ")

	answer, err := reader.ReadString('\n')
	if err != nil {
		return false
	}

	answer = strings.TrimSpace(strings.ToLower(answer))
	return answer == "y" || answer == "yes"
}

func promptElasticsearchCredentials() (string, string, error) {
	reader := bufio.NewReader(os.Stdin)

	fmt.Print("Elasticsearch username: ")
	username, err := reader.ReadString('\n')
	if err != nil {
		return "", "", err
	}
	username = strings.TrimSpace(username)
	if username == "" {
		return "", "", fmt.Errorf("elasticsearch username cannot be empty")
	}

	fmt.Print("Elasticsearch password: ")
	passwordBytes, err := term.ReadPassword(int(os.Stdin.Fd()))
	fmt.Println()
	if err != nil {
		return "", "", err
	}
	password := strings.TrimSpace(string(passwordBytes))
	if password == "" {
		return "", "", fmt.Errorf("elasticsearch password cannot be empty")
	}

	return username, password, nil
}
