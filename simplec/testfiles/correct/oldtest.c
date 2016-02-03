int c;
int g;
// laber;
int a(int param1, int param2){
    //int a;
    //int b;
    //Kommentar
    int a, t, s=777;
    int b = a;
    a = 5;
    # pragma hier
    /* Nochein Kommentar
     */
    if(a>= 0){
	a=33;
    }	else{
	a = 32;
    }
    return b;
}

int whileLoop(){
    int j;
    //int y;
    j = 0;
    int w=5;
    int y;
    y = a(j, w);
    while(j <= 10){
    y = y+2;
    j++;
    }
    j = y;
    return j;
}

int loop(){
    int i;
    int x;
    x= whileLoop();
    for(i = 0; i<=10; i=i+1){
	x = x+i;
	x=i;
    }
    i = x;
    x = a(i=i, x);
}

int e(){
    
    if ((c*25>=0 || c <= 3) && ! c++==g){
	 g= c;
    }
    
    


    c=5 + g;

}


int d(){
    int d;
    d=5;
    g=(44+ d)*c-(d-5);
    g=c+d;
    c=-d;
    return d;
}
