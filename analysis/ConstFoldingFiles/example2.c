
int main() {
    int i;
    int n;
    int m;

    for (i=0; i<5; ++i) {
        if (i == 4) {
            n = 43;
            m = 41;
        } else {
            n = 41;
            m = 43;
        }

        i = n + m;
    }

    return i;
}