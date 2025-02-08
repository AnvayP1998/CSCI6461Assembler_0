package com.myassembler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Assembler {
    public static void main(String[] args) {
        String inputFileName = "C:\\Users\\anvay\\Computer_System_Architecture\\CISC-Machine-Simulator-main\\CISC-Machine-Simulator-main\\P2\\src\\Resources\\input";
        String outputFileName = "C:\\Users\\anvay\\Computer_System_Architecture\\CISC-Machine-Simulator-main\\CISC-Machine-Simulator-main\\P2\\src\\Resources\\output";
        assemble(inputFileName, outputFileName);
    }

    public static void assemble(String inputFileName, String outputFileName) {
        Map<String, Integer> labels = new HashMap<>();
        int locationCounter = 0;

        // First pass: Build the label map
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.split("//")[0].trim();  // Remove comments
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");

                if (parts[0].endsWith(":")) {  // Label definition
                    String label = parts[0].substring(0, parts[0].length() - 1);
                    labels.put(label, locationCounter);
                } else {
                    locationCounter++;  // Regular instruction or data line
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        locationCounter = 0;

        // Second pass: Generate output with labels resolved
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.split("//")[0].trim();  // Remove comments
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");

                if (parts[0].equals("LOC")) {  // Set location counter
                    locationCounter = Integer.parseInt(parts[1], 16);
                    writer.write(String.format("%06o %06o LOC %s\n", locationCounter, 0, parts[1]));
                } else if (parts[0].equals("Data")) {  // Data handling
                    int value;
                    if (labels.containsKey(parts[1])) {
                        value = labels.get(parts[1]);  // Replace label with its location
                    } else {
                        try {
                            value = Integer.parseInt(parts[1], 10);  // Convert to integer
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid data or label: " + parts[1]);
                        }
                    }
                    writer.write(String.format("%06o %06o Data %s%n", locationCounter, value, parts[1]));
                    locationCounter++;
                } else {  // Instructions
                    int opcodeValue = getOpcodeValue(parts[0]);
                    String[] operands = parts.length > 1 ? parts[1].split(",") : new String[0];

                    // Operand initialization
                    int r = 0, ix = 0, i = 0, mem = 0;

                    try {
                        if (parts[0].equals("HLT")) {  // Special case for HLT (No operands)
                            writer.write(String.format("%06o %06o %s\n", locationCounter, 0, "HLT"));
                            locationCounter++;  // Increment the location counter
                            continue;  // Skip further processing for this line
                        }

                        // Handle operands only if not HLT
                        if (operands.length >= 1) r = parseOperand(operands[0], "r", 0, 3);
                        if (operands.length >= 2) ix = parseOperand(operands[1], "ix", 0, 3);
                        if (operands.length >= 3) mem = parseOperand(operands[2], "mem", 0, 1023);
                        if (operands.length == 4) i = parseOperand(operands[3], "i", 0, 1);

                        // Encoding instruction
                        int hexInstruction = (opcodeValue << 12) | (r << 10) | (ix << 8) | (i << 7) | mem;

                        writer.write(String.format("%06o %06o %s\n", locationCounter, hexInstruction, line));
                        locationCounter++;  // Increment the location counter after processing the instruction
                    } catch (Exception e) {
                        // Catch errors and print them in the output
                        writer.write(String.format("%06o Error: %s\n", locationCounter, e.getMessage()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getOpcodeValue(String opcode) {
        Map<String, Integer> opcodes = new HashMap<>();
        opcodes.put("LDR", 0x01);  // Load Register From Memory (opcode 01)
        opcodes.put("STR", 0x02);  // Store Register To Memory (opcode 02)
        opcodes.put("LDA", 0x03);  // Load Register with Address (opcode 03)
        opcodes.put("LDX", 0x41);  // Load Index Register from Memory (opcode 41)
        opcodes.put("STX", 0x42);  // Store Index Register to Memory (opcode 42)
        opcodes.put("JZ", 0x10);   // Jump if Zero (opcode 10)
        opcodes.put("HLT", 0x00);  // Halt (special case opcode 00)
        opcodes.put("JCC", 0x12);  // Jump Conditional (opcode 12)
        opcodes.put("IN", 0x61);   // Input (opcode 61)
        opcodes.put("OUT", 0x62);  // Output (opcode 62)

        return opcodes.getOrDefault(opcode, 0);  // Default to 0 if opcode not found
    }

    private static int parseOperand(String operand, String type, int min, int max) {
        try {
            int value = Integer.parseInt(operand);
            if (value < min || value > max) {
                throw new IllegalArgumentException(type + " operand out of range: " + operand);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + type + " operand: " + operand);
        }
    }
}
