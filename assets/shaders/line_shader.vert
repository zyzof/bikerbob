uniform mat4 uMvpMatrix;

attribute vec4 vPosition;

void main() {
	gl_Position = uMvpMatrix * vPosition;
}