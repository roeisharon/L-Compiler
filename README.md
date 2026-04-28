# L Language Compiler

A fully functional compiler for **L**, a statically-typed object-oriented programming language, built from scratch as part of the Compilation course (0368-3133) at Tel Aviv University. The compiler translates L source programs into MIPS assembly, running through all classical compiler phases.

---

## Overview

The compiler is implemented in Java and built incrementally across five exercises, each adding a new compilation phase:

| Exercise | Phase | Tool/Output |
|---|---|---|
| Ex1 | Lexical Analysis | JFlex scanner → tokenized output |
| Ex2 | Syntax Analysis | CUP parser → AST |
| Ex3 | Semantic Analysis | AST visitor → type-checked program |
| Ex4 | Data Flow Analysis | CFG + chaotic iterations → uninitialized variable report |
| Ex5 | Code Generation | IR → liveness analysis → register allocation → MIPS assembly |

---

## The L Language

L is a statically-typed, object-oriented language with the following features:

- **Primitive types**: `int` (16-bit signed, range 0–32767), `string`
- **Classes** with inheritance (`extends`), fields, and methods
- **Arrays** defined as named array types over any non-void element type
- **Control flow**: `if`/`else`, `while`
- **Functions** with typed parameters and return values
- **Operators**: arithmetic (`+`, `-`, `*`, `/`), comparison (`<`, `>`, `=`), assignment (`:=`)
- **Library functions**: `PrintInt`, `PrintString`
- **Runtime safety**: division by zero, null pointer dereference, and out-of-bounds array access are all caught at runtime

### Example L Program

```
int IsPrime(int p) {
    int i := 2;
    while (i < p) {
        int j := 2;
        while (j < p) {
            if (i * j = p) { return 0; }
            j := j + 1;
        }
        i := i + 1;
    }
    return 1;
}

void main() {
    int p := 2;
    while (p < 100) {
        if (IsPrime(p)) { PrintInt(p); }
        p := p + 1;
    }
}
```

---

## Compiler Phases

### Phase 1 – Lexical Analysis
A JFlex-based scanner tokenizes L source code. It recognizes identifiers, keywords, integer/string literals, operators, and punctuation. It rejects lexical errors including leading-zero integers, out-of-range integers, invalid string characters, and malformed comments.

### Phase 2 – Syntax Analysis
A CUP-based LALR(1) parser validates the token stream against L's grammar and builds an Abstract Syntax Tree (AST). Operator precedence and associativity (8 levels) are handled via CUP's declarative mechanism.

### Phase 3 – Semantic Analysis
An AST visitor performs:
- **Type checking** for all expressions, assignments, and function calls
- **Scope resolution** across global, class, function, and block scopes
- **Inheritance rules**: subtype substitution, method overriding (exact signature match required), shadowing detection
- **Array type non-interchangeability** enforcement
- **`nil` compatibility** with class and array types

### Phase 4 – Data Flow Analysis
A used-before-set analysis detects variables accessed without a guaranteed prior initialization:
1. The AST is lowered into a three-address IR with temporary variables
2. A Control Flow Graph (CFG) is built
3. Chaotic iteration computes which temporaries may be uninitialized at each program point
4. Results are mapped back to source-level variable names and reported

### Phase 5 – Code Generation
Full MIPS assembly generation from the L IR:
1. **IR generation**: AST → three-address IR with unbounded temporaries
2. **Liveness analysis**: live variable sets computed per CFG node
3. **Register allocation**: graph-coloring with simplification (10 physical registers: `$t0`–`$t9`); reports failure if spilling would be required
4. **MIPS emission**: IR commands translated to MIPS instructions; `$s0`–`$s9` used as scratch registers where needed
5. **Runtime checks**: div-by-zero, null dereference, and array out-of-bounds detected with appropriate error messages and graceful exit
6. **Saturation arithmetic**: integer operations clamp to [−32768, 32767]

---

## Project Structure

```
ex5/
├── Makefile
├── jflex/
│   └── LEX_FILE.lex          # Lexer specification
├── cup/
│   └── CUP_FILE.cup          # Parser grammar + AST construction
├── src/
│   ├── Main.java
│   ├── ast/                  # AST node classes
│   ├── types/                # Type system classes
│   ├── symboltable/          # Scoped symbol table
│   ├── ir/                   # IR commands, CFG, liveness, register allocator
│   ├── mips/                 # MIPS code emitter
│   └── temp/                 # Temporary variable factory
├── examples/                 # Sample L programs and their MIPS output
└── input/                    # Test cases
```

---

## Building and Running

### Prerequisites
- Java JDK 8+
- JFlex
- CUP (java-cup-11b)
- SPIM 8.0 (for running generated MIPS)

On Ubuntu/Debian:
```bash
sudo apt-get install spim graphviz
```

### Build
```bash
cd ex5
make
```

This generates the lexer and parser sources, then compiles everything into `ex5/COMPILER`.

### Usage
```bash
./COMPILER <input_file> <output_file>
```

The output file will contain either:
- The MIPS assembly translation of the program
- `ERROR` — lexical error
- `ERROR(N)` — syntax or semantic error on line N
- `Register Allocation Failed` — too many live temporaries to color with 10 registers

### Run with SPIM
```bash
spim -file output.s
```

---

## Language Semantics Highlights

**Saturation arithmetic**: Integer overflow clamps to the 16-bit signed boundary rather than wrapping.

```
int x := 32767;
int y := x + 1;   // y = 32767 (saturated)
```

**Subtype substitution**: A `Son` object can be passed wherever a `Father` is expected (but not vice versa, and not for arrays).

**`nil`**: Can be assigned to any class or array variable. Dereferencing `nil` triggers a runtime error.

**Left-to-right evaluation**: Function arguments are evaluated strictly left to right, which matters when arguments have side effects.

**Global initialization order**: Global variable initializers run in source order before `main` is entered.

---

## Runtime Errors

| Condition | Message |
|---|---|
| Division by zero | `Illegal Division By Zero` |
| Null pointer dereference | `Invalid Pointer Dereference` |
| Array index out of bounds | `Access Violation` |

All runtime errors print the message and exit the program cleanly.

---

## Test Cases

The `input/` directory contains a comprehensive test suite covering:
- Prime sieve, bubble sort, merge of linked lists, matrix operations
- String operations and concatenation
- Multi-level inheritance hierarchies
- Array access and equality
- Runtime error scenarios (div-by-zero, null deref, OOB)
- Evaluation order edge cases
- Register pressure tests (many local variables / data members)
- Fibonacci, overflow/saturation behavior

---

## Course

**Compilation** (0368-3133) — Tel Aviv University
