# Project2-CSCI6461-Team 8

# CSCI 6461 Assembler

## Overview
This repository contains an **Assembler** implementation for the **CSCI 6461 Computer System Architecture** course. The assembler translates assembly language code into machine code, enabling execution on a simulated CISC machine.

## Features
- **Two-Pass Assembly Process**:
  - **First Pass:** Identifies and maps label definitions to memory locations.
  - **Second Pass:** Translates assembly instructions into machine code.
- **Opcode Mapping**: Supports various assembly instructions with their corresponding opcodes.
- **File Handling**: Reads input assembly files and generates two output files:
  - **Listing File (`outputlisting`)**: Displays original assembly instructions along with machine code.
  - **Loading File (`output`)**: Contains machine code in octal format, ready for execution.
- **Error Handling**: Detects invalid instructions, operands, and memory addresses.

## Project Structure
```
|-- src/
|   |-- com/myassembler/
|   |   |-- Assembler.java
|-- resources/
|   |-- input (Sample Assembly Code)
|   |-- output (Generated Machine Code)
|   |-- outputlisting (Listing File)
|-- README.md
```

## Installation & Compilation
### Prerequisites
- Java Development Kit (JDK) 8 or later

### Compilation
Navigate to the source directory and compile the assembler using:
```bash
javac com/myassembler/Assembler.java
```

### Execution
Run the assembler with:
```bash
java Assembler.java
```

## Input Format
Assembly instructions must follow this format:
```
OPCODE REGISTER,INDEX,MEMORY_ADDRESS
```
**Example:**
```
LDR 2,1,5
STR 1,2,10
```

## Output Files
### Listing File (`outputlisting`)
Contains both assembly instructions and their corresponding machine code:
```
000100 001004 LDR 1,0,4
000101 002108 STR 2,1,8
```

### Loading File (`output`)
Contains only the machine code in octal format:
```
000100 001004
000101 002108
```

## Contributing
Contributions are welcome! If you'd like to improve this assembler, please submit a pull request.

## License
This project is licensed under the MIT License.
