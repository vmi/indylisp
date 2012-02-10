(define fizzbuzz (lambda (num max)
  (cond
    ((equals (remainder num 15) 0)
      (println out "FizzBuzz"))
    ((equals (remainder num 5) 0)
      (println out "Buzz"))
    ((equals (remainder num 3) 0)
      (println out "Fizz"))
    (t
      (println out num)))
  (cond
    ((equals (compareTo num max) -1)
      (fizzbuzz (add num 1) max)))))

(fizzbuzz 1 100)
