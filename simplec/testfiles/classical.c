void* malloc(int i);
int foo(){
  int i = 42;
  return i;
}

int (*g)();

void main(){
  int i;
  int* j;
  j = malloc(10);
  for (i=0;i<10;i++){
      if ((i/2)*2==i) continue;
      j[i]=i+1;
  }
  i = j[9];
  j = &i;
  i = *j;
  if (i==5) 
    foo(); // triggers a MethodCall Transition
  else    
    i=foo(); // triggers an Assignment with an embedded MethodCall Expression!
  while(i==4711) {
    i=42-42;
  }
}
