package kube

import (
	"fmt"
	"os"
	"os/exec"
)

func run(name string, args ...string) error {
	cmd := exec.Command(name, args...)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	return cmd.Run()
}

func CheckKubectl() error {
	_, err := exec.LookPath("kubectl")
	if err != nil {
		return fmt.Errorf("kubectl not found in PATH")
	}
	return nil
}

func HasClusterAccess() bool {
	cmd := exec.Command("kubectl", "cluster-info")
	return cmd.Run() == nil
}

func RequireClusterAccess() error {
	if !HasClusterAccess() {
		return fmt.Errorf("no reachable Kubernetes cluster found with current kubeconfig")
	}
	return nil
}

func EnsureNamespace(namespace string) error {
	cmd := exec.Command("kubectl", "get", "namespace", namespace)
	if err := cmd.Run(); err == nil {
		return nil
	}
	return run("kubectl", "create", "namespace", namespace)
}

func ApplyFile(namespace, path string) error {
	return run("kubectl", "-n", namespace, "apply", "-f", path)
}

func RolloutStatus(namespace, deployment string) error {
	return run("kubectl", "-n", namespace, "rollout", "status", "deployment/"+deployment, "--timeout=180s")
}

func GetStatus(namespace string) error {
	return run("kubectl", "-n", namespace, "get", "all")
}
