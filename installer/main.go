package main

import (
	"fmt"
	"os"

	"github.com/devopsnotes/k-guard-ai/installer/internal/cli"
)

func main() {
	// Default: TUI if no arguments.
	if len(os.Args) == 1 {
		if err := cli.RunTUI(); err != nil {
			fmt.Fprintf(os.Stderr, "error: %v\n", err)
			os.Exit(1)
		}
		return
	}

	// CLI mode: reuse existing commands (check, install, status)
	if err := cli.Run(os.Args[1:]); err != nil {
		fmt.Fprintf(os.Stderr, "error: %v\n", err)
		os.Exit(1)
	}
}
