;;; Function tests.

(cond
  ((car nil))
  (t
    (println out "OK - (car nil)")))

(cond
  ((cdr nil))
  (t
    (println out "OK - (cdr nil)")))


(define rmode-list (quote (
  UP
  DOWN
  CEILING
  FLOOR
  HALF_UP
  HALF_DOWN
  HALF_EVEN
  DECIMAL32
  DECIMAL64
  DECIMAL128
  UNLIMITED
)))

(define loop (lambda (r)
  (println out (car r))
  (define r (cdr r))
  (cond
    (r
      (loop r)))
))

(loop rmode-list)

(define divtest (lambda (num max)
  (printf out "%2.0f / 10 =" num)
  (define loop (lambda (r)
    (define m (car r))
    (printf out "%6.1f" (divide num 10 m))
    (define r (cdr r))
    (cond
      (r
        (loop r)))))
  (loop rmode-list)
  (println out)
  (cond
    ((equals (compareTo num max) -1)
      (divtest (add num 1) max)))))

(divtest 1 20)
