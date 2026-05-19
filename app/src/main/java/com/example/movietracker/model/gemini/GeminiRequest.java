package com.example.movietracker.model.gemini;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

public class GeminiRequest {
    @SerializedName("contents")
    private List<Content> contents;

    @SerializedName("systemInstruction")
    private SystemInstruction systemInstruction;

    public GeminiRequest() {
        this.contents = new ArrayList<>();
    }

    public void addContent(String role, String text) {
        Content content = new Content();
        content.setRole(role);
        Part part = new Part();
        part.setText(text);
        content.addPart(part);
        this.contents.add(content);
    }

    public void setSystemInstruction(String text) {
        this.systemInstruction = new SystemInstruction();
        Part part = new Part();
        part.setText(text);
        this.systemInstruction.addPart(part);
    }

    public static class Content {
        @SerializedName("role")
        private String role;

        @SerializedName("parts")
        private List<Part> parts;

        public Content() {
            this.parts = new ArrayList<>();
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void addPart(Part part) {
            this.parts.add(part);
        }
    }

    public static class SystemInstruction {
        @SerializedName("parts")
        private List<Part> parts;

        public SystemInstruction() {
            this.parts = new ArrayList<>();
        }

        public void addPart(Part part) {
            this.parts.add(part);
        }
    }

    public static class Part {
        @SerializedName("text")
        private String text;

        public void setText(String text) {
            this.text = text;
        }
    }
}
