int main(int n) {
	int x = 42;
	int r = x + 99 + 1; // true liveness test
	int i = 0;
	int y = 0;

    r = 1;

//	int r1;
//
//	if (i < 2) {
//	    r1 = y;
//	} else {
//	    int a = 10;
//	    r1 = a;
//	}

	while(i < 3) {
		r = r*n;
		y = y + 5;
		i = i + 1;
	}

    return r;
}