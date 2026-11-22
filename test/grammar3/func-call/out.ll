; ModuleID = 'spl'
source_filename = "spl"
target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128"
target triple = "x86_64-unknown-linux-gnu"

define i64 @double(i64 %0) {
entry:
  %v.x.1 = alloca i64, align 8
  store i64 %0, ptr %v.x.1, align 8
  %t.2 = load i64, ptr %v.x.1, align 8
  %t.3 = mul i64 %t.2, 2
  ret i64 %t.3
}

define i64 @main() {
entry:
  %v.y.4 = alloca i64, align 8
  %calltmp = call i64 @double(i64 21)
  store i64 %calltmp, ptr %v.y.4, align 8
  %t.5 = load i64, ptr %v.y.4, align 8
  ret i64 %t.5
}

