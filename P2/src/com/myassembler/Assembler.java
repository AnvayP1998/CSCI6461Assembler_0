package com.myassembler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Assembler {
    public static void main(String[] args) {
        String inputFileName = "C:\\Users\\anvay\\Computer_System_Architecture\\CISC-Machine-Simulator-main\\CISC-Machine-Simulator-main\\P2\\src\\Resources\\input";
        String outputFileName = "C:\\Users\\anvay\\Computer_System_Architecture\\CISC-Machine-Simulator-main\\CISC-Machine-Simulator-main\\P2\\src\\Resources\\output";
        String outputListingFileName = "C:\\Users\\anvay\\Computer_System_Architecture\\CISC-Machine-Simulator-main\\CISC-Machine-Simulator-main\\P2\\src\\Resources\\outputlisting";
        assemble(inputFileName, outputFileName, outputListingFileName);
    }

    public static void assemble(String inputFileName, String outputFileName, String outputListingFileName) {
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
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
             BufferedWriter listingWriter = new BufferedWriter(new FileWriter(outputListingFileName))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.split("//")[0].trim();  // Remove comments
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");

                if (parts[0].equals("LOC")) {
                    locationCounter = Integer.parseInt(parts[1], 16);
                    writer.write(String.format("%06o %06o\n", locationCounter, 0));
                    listingWriter.write(String.format("%06o %06o LOC %d\n", locationCounter, 0, locationCounter));
                    continue;
                } else if (parts[0].equals("Data")) {  // Data handling
                    int value;
                    if (labels.containsKey(parts[1])) {
                        value = labels.get(parts[1]);
                    } else {
                        try {
                            value = Integer.parseInt(parts[1], 10);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid data or label: " + parts[1]);
                        }
                    }
                    writer.write(String.format("%06o %06o\n", locationCounter, value));
                    listingWriter.write(String.format("%06o %06o Data %d\n", locationCounter, value, value));
                    locationCounter++;
                } else {
                    int opcodeValue = getOpcodeValue(parts[0]);
                    String[] operands = parts.length > 1 ? parts[1].split(",") : new String[0];

                    int r = 0, ix = 0, i = 0, mem = 0;

                    try {
                        if (parts[0].equals("HLT")) {  // Special case for HLT (no operands)
                            writer.write(String.format("%06o %06o\n", locationCounter, 0));
                            listingWriter.write(String.format("%06o %06o HLT\n", locationCounter, 0));
                            locationCounter++;
                            continue;
                        }

                        if (operands.length >= 1) r = parseOperand(operands[0], "r", 0, 3);
                        if (operands.length >= 2) ix = parseOperand(operands[1], "ix", 0, 3);
                        if (operands.length >= 3) mem = parseOperand(operands[2], "mem", 0, 1023);
                        if (operands.length == 4) i = parseOperand(operands[3], "i", 0, 1);

                        int hexInstruction = (opcodeValue << 12) | (r << 10) | (ix << 8) | (i << 7) | mem;

                        writer.write(String.format("%06o %06o\n", locationCounter, hexInstruction));
                        listingWriter.write(String.format("%06o %06o %s %s\n", locationCounter, hexInstruction, parts[0], String.join(",", operands)));
                        locationCounter++;
                    } catch (Exception e) {
                        writer.write(String.format("%06o Error: %s\n", locationCounter, e.getMessage()));
                        listingWriter.write(String.format("%06o Error: %s\n", locationCounter, e.getMessage()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getOpcodeValue(String opcode) {
        Map<String, Integer> opcodes = new HashMap<>();
        opcodes.put("LDR", 0x01);  // Load Register From Memory
        opcodes.put("STR", 0x02);  // Store Register To Memory
        opcodes.put("LDA", 0x03);  // Load Register with Address
        opcodes.put("LDX", 0x41);  // Load Index Register from Memory
        opcodes.put("STX", 0x42);  // Store Index Register to Memory
        opcodes.put("JZ", 0x10);   // Jump if Zero
        opcodes.put("HLT", 0x00);  // Halt
        opcodes.put("JCC", 0x12);  // Jump Conditional
        opcodes.put("IN", 0x61);   // Input
        opcodes.put("OUT", 0x62);  // Output

        return opcodes.getOrDefault(opcode, 0);
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
