package main

import (
    "fmt"
    "os"

    "github.com/devopsnotes/k-guard-ai/installer/internal/cli"
)

func main() {
    // Si aucune commande n’est fournie, lancer le TUI.
    if len(os.Args) <= 1 {
        if err := cli.RunTUI(); err != nil {
            fmt.Fprintf(os.Stderr, "error: %v\n", err)
            os.Exit(1)
        }
        return
    }

    // Sinon conserver le comportement CLI classique.
    if err := cli.Run(os.Args[1:]); err != nil {
        fmt.Fprintf(os.Stderr, "error: %v\n", err)
        os.Exit(1)
    }
}
