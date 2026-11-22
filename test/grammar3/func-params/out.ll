; ModuleID = 'spl'
source_filename = "spl"
target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128"
target triple = "x86_64-unknown-linux-gnu"

define i64 @add(i64 %0, i64 %1) {
entry:
  %v.a.1 = alloca i64, align 8
  %v.b.2 = alloca i64, align 8
  %v.result.6 = alloca i64, align 8
  store i64 %0, ptr %v.a.1, align 8
  store i64 %1, ptr %v.b.2, align 8
  %t.3 = load i64, ptr %v.a.1, align 8
  %t.4 = load i64, ptr %v.b.2, align 8
  %t.5 = add i64 %t.3, %t.4
  store i64 %t.5, ptr %v.result.6, align 8
  %t.7 = load i64, ptr %v.result.6, align 8
  ret i64 %t.7
}

define i64 @main() {
entry:
  %calltmp = call i64 @add(i64 2, i64 3)
  ret i64 %calltmp
}

