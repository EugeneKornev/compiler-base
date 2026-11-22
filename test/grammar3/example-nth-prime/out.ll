; ModuleID = 'spl'
source_filename = "spl"
target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128"
target triple = "x86_64-unknown-linux-gnu"

declare i64 @putchar(i64)

define i64 @printInt(i64 %0) {
entry:
  %v.n.1 = alloca i64, align 8
  store i64 %0, ptr %v.n.1, align 8
  %t.2 = load i64, ptr %v.n.1, align 8
  %t.3 = icmp slt i64 %t.2, 0
  %t.4 = zext i1 %t.3 to i64
  %t.5 = icmp ne i64 %t.4, 0
  br i1 %t.5, label %if.then.1, label %if.else.3
if.then.1:
  %calltmp = call i64 @putchar(i64 45)
  %t.6 = load i64, ptr %v.n.1, align 8
  %t.7 = sub i64 0, %t.6
  %calltmp1 = call i64 @printInt(i64 %t.7)
  br label %if.end.2
if.else.3:
  %t.8 = load i64, ptr %v.n.1, align 8
  %t.9 = icmp sge i64 %t.8, 10
  %t.10 = zext i1 %t.9 to i64
  %t.11 = icmp ne i64 %t.10, 0
  br i1 %t.11, label %if.then.4, label %if.end.5
if.then.4:
  %t.12 = load i64, ptr %v.n.1, align 8
  %t.13 = sdiv i64 %t.12, 10
  %calltmp2 = call i64 @printInt(i64 %t.13)
  br label %if.end.5
if.end.5:
  %t.14 = load i64, ptr %v.n.1, align 8
  %t.15 = srem i64 %t.14, 10
  %t.16 = add i64 48, %t.15
  %calltmp3 = call i64 @putchar(i64 %t.16)
  br label %if.end.2
if.end.2:
  ret i64 0
}

define i64 @isPrime(i64 %0) {
entry:
  %v.n.17 = alloca i64, align 8
  %v.i.22 = alloca i64, align 8
  store i64 %0, ptr %v.n.17, align 8
  %t.18 = load i64, ptr %v.n.17, align 8
  %t.19 = icmp slt i64 %t.18, 2
  %t.20 = zext i1 %t.19 to i64
  %t.21 = icmp ne i64 %t.20, 0
  br i1 %t.21, label %if.then.6, label %if.end.7
if.then.6:
  ret i64 0
if.end.7:
  store i64 2, ptr %v.i.22, align 8
  br label %while.cond.8
while.cond.8:
  %t.23 = load i64, ptr %v.i.22, align 8
  %t.24 = load i64, ptr %v.i.22, align 8
  %t.25 = mul i64 %t.23, %t.24
  %t.26 = load i64, ptr %v.n.17, align 8
  %t.27 = icmp sle i64 %t.25, %t.26
  %t.28 = zext i1 %t.27 to i64
  %t.29 = icmp ne i64 %t.28, 0
  br i1 %t.29, label %while.body.9, label %while.end.10
while.body.9:
  %t.30 = load i64, ptr %v.n.17, align 8
  %t.31 = load i64, ptr %v.i.22, align 8
  %t.32 = srem i64 %t.30, %t.31
  %t.33 = icmp eq i64 %t.32, 0
  %t.34 = zext i1 %t.33 to i64
  %t.35 = icmp ne i64 %t.34, 0
  br i1 %t.35, label %if.then.11, label %if.end.12
if.then.11:
  ret i64 0
if.end.12:
  %t.36 = load i64, ptr %v.i.22, align 8
  %t.37 = add i64 %t.36, 1
  store i64 %t.37, ptr %v.i.22, align 8
  br label %while.cond.8
while.end.10:
  ret i64 1
}

define i64 @main() {
entry:
  %v.count.38 = alloca i64, align 8
  %v.n.39 = alloca i64, align 8
  store i64 0, ptr %v.count.38, align 8
  store i64 1, ptr %v.n.39, align 8
  br label %while.cond.13
while.cond.13:
  %t.40 = load i64, ptr %v.count.38, align 8
  %t.41 = icmp slt i64 %t.40, 10
  %t.42 = zext i1 %t.41 to i64
  %t.43 = icmp ne i64 %t.42, 0
  br i1 %t.43, label %while.body.14, label %while.end.15
while.body.14:
  %t.44 = load i64, ptr %v.n.39, align 8
  %t.45 = add i64 %t.44, 1
  store i64 %t.45, ptr %v.n.39, align 8
  %t.46 = load i64, ptr %v.n.39, align 8
  %calltmp = call i64 @isPrime(i64 %t.46)
  %t.47 = icmp ne i64 %calltmp, 0
  br i1 %t.47, label %if.then.16, label %if.end.17
if.then.16:
  %t.48 = load i64, ptr %v.count.38, align 8
  %t.49 = add i64 %t.48, 1
  store i64 %t.49, ptr %v.count.38, align 8
  br label %if.end.17
if.end.17:
  br label %while.cond.13
while.end.15:
  %t.50 = load i64, ptr %v.n.39, align 8
  %calltmp1 = call i64 @printInt(i64 %t.50)
  %calltmp2 = call i64 @putchar(i64 10)
  ret i64 0
}

