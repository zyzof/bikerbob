precision mediump float;

uniform vec4 uColor;
uniform sampler2D uTexture;

varying vec2 vTexCoord;	// Interpolated texture coordinate per fragment.

void main() {
    gl_FragColor = texture2D(uTexture, vTexCoord);
}