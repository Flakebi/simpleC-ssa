int odd(int);
int even(int a){

  if(a == 0) return 1;

  else return odd(a-1);

}

int odd(int a){

            if(a == 1) return 0;

            else return even(a-1);
}
