
int main() {
    int a = 3;
    int b = 0;
    int c = 2;

    int d = a / c;
    int e = d / b;

    int i;
    for (i=-2; i<=2; i++) {
        e = e / i;
    }

    return e;
}