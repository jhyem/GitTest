#version 300 es
precision highp float;

uniform vec4 u_Color;

out vec4 o_FragColor;

void main() {
    o_FragColor = u_Color;
} 