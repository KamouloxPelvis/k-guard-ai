package kube

import (
	"bytes"
	"fmt"
	"os"
	"os/exec"
)

func ApplyElasticsearchSecret(namespace, secretName, username, password string) error {
	createCmd := exec.Command(
		"kubectl", "-n", namespace, "create", "secret", "generic", secretName,
		"--from-literal=ELASTICSEARCH_USERNAME="+username,
		"--from-literal=ELASTICSEARCH_PASSWORD="+password,
		"--dry-run=client",
		"-o", "yaml",
	)
	createCmd.Env = os.Environ()

	out, err := createCmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("failed to generate Kubernetes secret manifest: %s", string(out))
	}

	applyCmd := exec.Command("kubectl", "-n", namespace, "apply", "-f", "-")
	applyCmd.Env = os.Environ()
	applyCmd.Stdin = bytes.NewReader(out)
	applyCmd.Stdout = os.Stdout
	applyCmd.Stderr = os.Stderr

	if err := applyCmd.Run(); err != nil {
		return fmt.Errorf("failed to apply Kubernetes secret: %w", err)
	}

	return nil
}
