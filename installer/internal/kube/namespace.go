package kube

import "os/exec"

func NamespaceExists(namespace string) bool {
	cmd := exec.Command("kubectl", "get", "namespace", namespace, "-o", "name")
	return cmd.Run() == nil
}

func DetectPreferredNamespace() string {
	if NamespaceExists("k-guard") {
		return "k-guard"
	}
	return ""
}
