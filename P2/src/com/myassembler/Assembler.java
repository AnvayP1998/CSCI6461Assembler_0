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
                line = line.split("//")[0].trim();  // Strip out comments
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");

                // Handle labels (end with ":")
                if (parts[0].endsWith(":")) {
                    String label = parts[0].substring(0, parts[0].length() - 1);
                    labels.put(label, locationCounter);
                } else {
                    locationCounter++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        locationCounter = 0;

        // Second pass: Generate output
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
             BufferedWriter listingWriter = new BufferedWriter(new FileWriter(outputListingFileName))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.split("//")[0].trim();  // Strip out comments
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");

                if (parts[0].equals("LOC")) {
                    locationCounter = Integer.parseInt(parts[1], 10);
                    continue;
                }

                if (parts[0].equals("Data")) {
                    int value = resolveValue(parts[1], labels);
                    writer.write(String.format("%06o %06o\n", locationCounter, value));
                    listingWriter.write(String.format("%06o %06o Data %s\n", locationCounter, value, parts[1]));
                    locationCounter++;
                    continue;
                }

                int opcodeValue = getOpcodeValue(parts[0]);
                String[] operands = parts.length > 1 ? parts[1].split(",") : new String[0];
                int r = 0, ix = 0, i = 0, mem = 0;

                try {
                    // Special handling for HLT instruction to avoid shifting opcode
                    if (parts[0].equals("HLT")) {
                        writer.write(String.format("%06o %06o\n", locationCounter, opcodeValue << 12));
                        listingWriter.write(String.format("%06o %06o HLT\n", locationCounter, opcodeValue << 12));
                        locationCounter++;
                        continue;
                    }

                    // Parsing operands for instructions like LDX, LDR, LDA
                    if (operands.length >= 1) r = parseOperand(operands[0], "r", 0, 3);
                    if (operands.length >= 2) ix = parseOperand(operands[1], "ix", 0, 3);
                    if (operands.length >= 3) mem = resolveValue(operands[2], labels);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getOpcodeValue(String opcode) {
        Map<String, Integer> opcodes = new HashMap<>();
        opcodes.put("LDR", 0x01);
        opcodes.put("STR", 0x02);
        opcodes.put("LDA", 0x03);
        opcodes.put("LDX", 0x41);
        opcodes.put("STX", 0x42);
        opcodes.put("JZ", 0x10);
        opcodes.put("HLT", 0x00);  // Special handling for HLT
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

    private static int resolveValue(String operand, Map<String, Integer> labels) {
        if (labels.containsKey(operand)) {
            return labels.get(operand);
        }
        try {
            return Integer.parseInt(operand);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid data or label: " + operand);
        }
    }
}
