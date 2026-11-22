; ModuleID = 'spl'
source_filename = "spl"
target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128"
target triple = "x86_64-unknown-linux-gnu"

define i64 @fib(i64 %0) {
entry:
  %v.n.1 = alloca i64, align 8
  store i64 %0, ptr %v.n.1, align 8
  %t.2 = load i64, ptr %v.n.1, align 8
  %t.3 = icmp sle i64 %t.2, 1
  %t.4 = zext i1 %t.3 to i64
  %t.5 = icmp ne i64 %t.4, 0
  br i1 %t.5, label %if.then.1, label %if.end.2
if.then.1:
  %t.6 = load i64, ptr %v.n.1, align 8
  ret i64 %t.6
if.end.2:
  %t.7 = load i64, ptr %v.n.1, align 8
  %t.8 = sub i64 %t.7, 1
  %calltmp = call i64 @fib(i64 %t.8)
  %t.9 = load i64, ptr %v.n.1, align 8
  %t.10 = sub i64 %t.9, 2
  %calltmp1 = call i64 @fib(i64 %t.10)
  %t.11 = add i64 %calltmp, %calltmp1
  ret i64 %t.11
}

declare i64 @getchar()

define i64 @main() {
entry:
  %v.result.12 = alloca i64, align 8
  %calltmp = call i64 @fib(i64 10)
  store i64 %calltmp, ptr %v.result.12, align 8
  %t.13 = load i64, ptr %v.result.12, align 8
  ret i64 %t.13
}

