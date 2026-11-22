; ModuleID = 'spl'
source_filename = "spl"
target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128"
target triple = "x86_64-unknown-linux-gnu"

define i64 @add(i64 %0, i64 %1) {
entry:
  %v.a.1 = alloca i64, align 8
  %v.b.2 = alloca i64, align 8
  store i64 %0, ptr %v.a.1, align 8
  store i64 %1, ptr %v.b.2, align 8
  %t.3 = load i64, ptr %v.a.1, align 8
  %t.4 = load i64, ptr %v.b.2, align 8
  %t.5 = add i64 %t.3, %t.4
  ret i64 %t.5
}

define i64 @main() {
entry:
  %v.x.6 = alloca i64, align 8
  %calltmp = call i64 @add(i64 1, i64 2)
  %calltmp1 = call i64 @add(i64 3, i64 4)
  %calltmp2 = call i64 @add(i64 %calltmp, i64 %calltmp1)
  store i64 %calltmp2, ptr %v.x.6, align 8
  %t.7 = load i64, ptr %v.x.6, align 8
  ret i64 %t.7
}

