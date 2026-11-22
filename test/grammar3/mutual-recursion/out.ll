; ModuleID = 'spl'
source_filename = "spl"
target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128"
target triple = "x86_64-unknown-linux-gnu"

define i64 @is_even(i64 %0) {
entry:
  %v.n.1 = alloca i64, align 8
  store i64 %0, ptr %v.n.1, align 8
  %t.2 = load i64, ptr %v.n.1, align 8
  %t.3 = icmp eq i64 %t.2, 0
  %t.4 = zext i1 %t.3 to i64
  %t.5 = icmp ne i64 %t.4, 0
  br i1 %t.5, label %if.then.1, label %if.end.2
if.then.1:
  ret i64 1
if.end.2:
  %t.6 = load i64, ptr %v.n.1, align 8
  %t.7 = sub i64 %t.6, 1
  %calltmp = call i64 @is_odd(i64 %t.7)
  ret i64 %calltmp
}

define i64 @is_odd(i64 %0) {
entry:
  %v.n.8 = alloca i64, align 8
  store i64 %0, ptr %v.n.8, align 8
  %t.9 = load i64, ptr %v.n.8, align 8
  %t.10 = icmp eq i64 %t.9, 0
  %t.11 = zext i1 %t.10 to i64
  %t.12 = icmp ne i64 %t.11, 0
  br i1 %t.12, label %if.then.3, label %if.end.4
if.then.3:
  ret i64 0
if.end.4:
  %t.13 = load i64, ptr %v.n.8, align 8
  %t.14 = sub i64 %t.13, 1
  %calltmp = call i64 @is_even(i64 %t.14)
  ret i64 %calltmp
}

define i64 @main() {
entry:
  %calltmp = call i64 @is_even(i64 4)
  ret i64 %calltmp
}

