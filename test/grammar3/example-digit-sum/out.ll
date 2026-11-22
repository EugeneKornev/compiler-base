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

define i64 @digitSum(i64 %0) {
entry:
  %v.n.17 = alloca i64, align 8
  %v.s.18 = alloca i64, align 8
  %v.v.20 = alloca i64, align 8
  store i64 %0, ptr %v.n.17, align 8
  store i64 0, ptr %v.s.18, align 8
  %t.19 = load i64, ptr %v.n.17, align 8
  store i64 %t.19, ptr %v.v.20, align 8
  br label %while.cond.6
while.cond.6:
  %t.21 = load i64, ptr %v.v.20, align 8
  %t.22 = icmp sgt i64 %t.21, 0
  %t.23 = zext i1 %t.22 to i64
  %t.24 = icmp ne i64 %t.23, 0
  br i1 %t.24, label %while.body.7, label %while.end.8
while.body.7:
  %t.25 = load i64, ptr %v.s.18, align 8
  %t.26 = load i64, ptr %v.v.20, align 8
  %t.27 = srem i64 %t.26, 10
  %t.28 = add i64 %t.25, %t.27
  store i64 %t.28, ptr %v.s.18, align 8
  %t.29 = load i64, ptr %v.v.20, align 8
  %t.30 = sdiv i64 %t.29, 10
  store i64 %t.30, ptr %v.v.20, align 8
  br label %while.cond.6
while.end.8:
  %t.31 = load i64, ptr %v.s.18, align 8
  ret i64 %t.31
}

define i64 @main() {
entry:
  %calltmp = call i64 @digitSum(i64 12345)
  %calltmp1 = call i64 @printInt(i64 %calltmp)
  %calltmp2 = call i64 @putchar(i64 10)
  ret i64 0
}

