
int main() {
    int x = 2;
    int y = 3;
    int i;
    int xx = x * 34;
    for (i = 0; i < 4; ++i) {
        x = y * (x + i + y);
        y = 3;
    }
    return x + xx;
}
