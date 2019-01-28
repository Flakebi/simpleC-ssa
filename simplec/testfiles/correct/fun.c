int j = 0;

int f(int i) {
    j = 55;
    return 32;
}

int main() {
    j = 7;
    int x = 77;
    int a = f();
    int b = 2;

    return x + a + b + j;
}