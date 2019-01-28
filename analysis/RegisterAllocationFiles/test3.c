int main() {
    int t = 0;
    int x = t;
    int y = 1;
    int z = x + y;
    x = x + 1;
    int q = x + y;
    y = q + z;
    t = t + 1;
    x = x + 1;
    int r = y + t;
    int ret = x + r;
    return ret;
}
