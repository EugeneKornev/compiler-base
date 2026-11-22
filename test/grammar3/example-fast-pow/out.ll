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

define i64 @fastPow(i64 %0, i64 %1) {
entry:
  %v.base.17 = alloca i64, align 8
  %v.exp.18 = alloca i64, align 8
  %v.result.19 = alloca i64, align 8
  %v.b.21 = alloca i64, align 8
  %v.e.23 = alloca i64, align 8
  store i64 %0, ptr %v.base.17, align 8
  store i64 %1, ptr %v.exp.18, align 8
  store i64 1, ptr %v.result.19, align 8
  %t.20 = load i64, ptr %v.base.17, align 8
  store i64 %t.20, ptr %v.b.21, align 8
  %t.22 = load i64, ptr %v.exp.18, align 8
  store i64 %t.22, ptr %v.e.23, align 8
  br label %while.cond.6
while.cond.6:
  %t.24 = load i64, ptr %v.e.23, align 8
  %t.25 = icmp sgt i64 %t.24, 0
  %t.26 = zext i1 %t.25 to i64
  %t.27 = icmp ne i64 %t.26, 0
  br i1 %t.27, label %while.body.7, label %while.end.8
while.body.7:
  %t.28 = load i64, ptr %v.e.23, align 8
  %t.29 = srem i64 %t.28, 2
  %t.30 = icmp eq i64 %t.29, 1
  %t.31 = zext i1 %t.30 to i64
  %t.32 = icmp ne i64 %t.31, 0
  br i1 %t.32, label %if.then.9, label %if.end.10
if.then.9:
  %t.33 = load i64, ptr %v.result.19, align 8
  %t.34 = load i64, ptr %v.b.21, align 8
  %t.35 = mul i64 %t.33, %t.34
  store i64 %t.35, ptr %v.result.19, align 8
  br label %if.end.10
if.end.10:
  %t.36 = load i64, ptr %v.b.21, align 8
  %t.37 = load i64, ptr %v.b.21, align 8
  %t.38 = mul i64 %t.36, %t.37
  store i64 %t.38, ptr %v.b.21, align 8
  %t.39 = load i64, ptr %v.e.23, align 8
  %t.40 = sdiv i64 %t.39, 2
  store i64 %t.40, ptr %v.e.23, align 8
  br label %while.cond.6
while.end.8:
  %t.41 = load i64, ptr %v.result.19, align 8
  ret i64 %t.41
}

define i64 @main() {
entry:
  %calltmp = call i64 @fastPow(i64 3, i64 13)
  %calltmp1 = call i64 @printInt(i64 %calltmp)
  %calltmp2 = call i64 @putchar(i64 10)
  ret i64 0
}

