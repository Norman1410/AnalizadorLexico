.data
nl: .asciiz "\n"
.align 2
g_gW: .word 100
.align 2
g_gF: .float 3.5
.align 2
g_gB: .word 1
.align 2
g_gC: .word 90
.align 2
str_0: .asciiz "ChipLang"
g_gS: .word str_0
.align 2
g_A: .word 0:8
.align 2
g_C: .byte 0:5
flt_0: .float 2.0
flt_1: .float 1.0
str_1: .asciiz "===== START TEST SUITE ====="
str_2: .asciiz "== GLOBALES =="
str_3: .asciiz "== ARRAY A (antes) =="
str_4: .asciiz "== ARRAY C (chars) =="
str_5: .asciiz "== ARIT INT =="
str_6: .asciiz "x"
str_7: .asciiz "y"
str_8: .asciiz "x+y"
str_9: .asciiz "x-y"
str_10: .asciiz "x*y"
str_11: .asciiz "17//5 (div entera)"
str_12: .asciiz "17%5"
str_13: .asciiz "2^5"
str_14: .asciiz "(2+3)*4 (paréntesis ¿ ? en tu lenguaje; aquí uso () como agrupación visual)"
str_15: .asciiz "negativo literal -7"
str_16: .asciiz "++x (prefijo)"
str_17: .asciiz "x (después de ++x)"
str_18: .asciiz "--x (prefijo)"
str_19: .asciiz "x (después de --x)"
str_20: .asciiz "== ARIT FLOAT =="
flt_9: .float 5.0
str_21: .asciiz "f"
str_22: .asciiz "f/2.0"
flt_10: .float 2.0
str_23: .asciiz "++f"
flt_11: .float 1.0
str_24: .asciiz "f (después de ++f)"
str_25: .asciiz "poly(3.0) = x^2+2x+1"
flt_12: .float 3.0
str_26: .asciiz "== RELACIONALES =="
str_27: .asciiz "3 < 5"
str_28: .asciiz "3 <= 3"
str_29: .asciiz "5 > 9"
str_30: .asciiz "5 != 9"
str_31: .asciiz "true == false"
str_32: .asciiz "true != false"
str_33: .asciiz "== LOGICAS =="
str_34: .asciiz "b1"
str_35: .asciiz "b2"
str_36: .asciiz "b1 @ b2"
str_37: .asciiz "b1 ~ b2"
str_38: .asciiz "Σ b1"
str_39: .asciiz "negB(false)"
str_40: .asciiz "== DECIDE OF =="
str_41: .asciiz "v"
str_42: .asciiz "BRANCH: v < 0"
str_43: .asciiz "BRANCH: 0 <= v < 10"
str_44: .asciiz "BRANCH: v >= 10"
str_45: .asciiz "== LOOP / EXIT WHEN / BREAK =="
str_46: .asciiz "BREAK@i==3"
str_47: .asciiz "== FUNCIONES =="
str_48: .asciiz "sum3(1,2,3)"
str_49: .asciiz "isEven(10)"
str_50: .asciiz "isEven(11)"
str_51: .asciiz "expresión sin asignación: (8+2)*3"
str_52: .asciiz "== SUMA ARRAY A =="
str_53: .asciiz "suma(A)"
str_54: .asciiz "modifico A[0][0] = A[0][0] + 100"
str_55: .asciiz "===== END TEST SUITE ====="

.text
.globl main


sum3:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -0
    lw $t0, 4($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, 8($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, 12($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    move $v0, $t0
    j exit_func_sum3
exit_func_sum3:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    jr $ra

poly:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -0
    l.s $f0, 4($fp)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, 4($fp)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    mul.s $f0, $f0, $f1
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, flt_0
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, 4($fp)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    mul.s $f0, $f0, $f1
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    add.s $f0, $f0, $f1
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, flt_1
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    add.s $f0, $f0, $f1
    j exit_func_poly
exit_func_poly:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    jr $ra

isEven:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -0
    lw $t0, 4($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 2
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    div $t0, $t1
    mfhi $t0
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    move $v0, $t0
    j exit_func_isEven
exit_func_isEven:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    jr $ra

negB:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -0
    lw $t0, 4($fp)
    xori $t0, $t0, 1
    move $v0, $t0
    j exit_func_negB
exit_func_negB:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    jr $ra
main:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -40
    la $t0, str_1
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_2
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, g_gW
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, g_gF
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, g_gB
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, g_gC
    li $v0, 11
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, g_gS
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 0
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 9
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 1
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 1
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 2
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 4
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 3
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 2
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 4
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 8
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 5
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 7
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 6
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 5
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 7
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 3
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    la $t1, g_C
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 0
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 5
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 104
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sb $t0, 0($t1)
    la $t1, g_C
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 1
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 5
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 111
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sb $t0, 0($t1)
    la $t1, g_C
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 2
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 5
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 108
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sb $t0, 0($t1)
    la $t1, g_C
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 3
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 5
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 97
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sb $t0, 0($t1)
    la $t1, g_C
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 4
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 5
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 33
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sb $t0, 0($t1)
    la $t0, str_3
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 0
    sw $t0, -8($fp)
loop_start_3:
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -8($fp)
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    lw $t0, 0($t1)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    sw $t0, -8($fp)
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 8
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, loop_end_4
    j loop_start_3
loop_end_4:
    la $t0, str_4
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 0
    sw $t0, -8($fp)
loop_start_5:
    la $t1, g_C
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -8($fp)
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 5
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    addu $t1, $t1, $t2
    lb $t0, 0($t1)
    li $v0, 11
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    sw $t0, -8($fp)
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 5
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, loop_end_6
    j loop_start_5
loop_end_6:
    la $t0, str_5
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 5
    sw $t0, -12($fp)
    li $t0, 2
    sw $t0, -16($fp)
    la $t0, str_6
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_7
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -16($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_8
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, -16($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_9
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, -16($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    sub $t0, $t0, $t1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_10
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, -16($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    mul $t0, $t0, $t1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_11
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 17
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 5
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    la $t0, str_12
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 17
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 5
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    div $t0, $t1
    mfhi $t0
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_13
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 2
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 5
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    li $t2, 1
pow_loop_7:
    beq $t1, $zero, pow_end_8
    mul $t2, $t2, $t0
    addi $t1, $t1, -1
    j pow_loop_7
pow_end_8:
    move $t0, $t2
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_14
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 2
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 3
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 4
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    mul $t0, $t0, $t1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_15
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 7
    neg $t0, $t0
    sw $t0, -20($fp)
    lw $t0, -20($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_16
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    addi $t1, $fp, -12
    lw $t0, 0($t1)
    addi $t0, $t0, 1
    sw $t0, 0($t1)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_17
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_18
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    addi $t1, $fp, -12
    lw $t0, 0($t1)
    addi $t0, $t0, -1
    sw $t0, 0($t1)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_19
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_20
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, flt_9
    s.s $f0, -24($fp)
    la $t0, str_21
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -24($fp)
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_22
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -24($fp)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, flt_10
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    div.s $f0, $f0, $f1
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_23
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -24($fp)
    addi $t1, $fp, -24
    l.s $f0, 0($t1)
    l.s $f1, flt_11
    add.s $f0, $f0, $f1
    s.s $f0, 0($t1)
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_24
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -24($fp)
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_25
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, flt_12
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    jal poly
    addi $sp, $sp, 4
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_26
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_27
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 3
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 5
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_28
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 3
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 3
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t1, $t0
    xori $t0, $t0, 1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_29
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 5
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 9
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t1, $t0
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_30
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 5
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 9
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltu $t0, $zero, $t0
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_31
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 1
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_32
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 1
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltu $t0, $zero, $t0
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_33
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 1
    sw $t0, -28($fp)
    li $t0, 0
    sw $t0, -32($fp)
    la $t0, str_34
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -28($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_35
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -32($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_36
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -28($fp)
    beq $t0, $zero, logic_end_13
    lw $t0, -32($fp)
logic_end_13:
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_37
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -28($fp)
    bne $t0, $zero, logic_end_14
    lw $t0, -32($fp)
logic_end_14:
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_38
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -28($fp)
    xori $t0, $t0, 1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_39
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 0
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    jal negB
    addi $sp, $sp, 4
    move $t0, $v0
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_40
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 7
    sw $t0, -36($fp)
    la $t0, str_41
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -36($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -36($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    beq $t0, $zero, decide_next_16
    la $t0, str_42
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_15
decide_next_16:
    lw $t0, -36($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, and_rhs_18
    j decide_next_17
and_rhs_18:
    lw $t0, -36($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 10
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    beq $t0, $zero, decide_next_17
    la $t0, str_43
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_15
decide_next_17:
    la $t0, str_44
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
decide_end_15:
    la $t0, str_45
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 0
    sw $t0, -8($fp)
loop_start_19:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 3
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    beq $t0, $zero, decide_next_22
    la $t0, str_46
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j loop_end_20
    j decide_end_21
decide_next_22:
    lw $t0, -8($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
decide_end_21:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    sw $t0, -8($fp)
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 10
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, loop_end_20
    j loop_start_19
loop_end_20:
    la $t0, str_47
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_48
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 3
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 2
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    jal sum3
    addi $sp, $sp, 12
    move $t0, $v0
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_49
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 10
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    jal isEven
    addi $sp, $sp, 4
    move $t0, $v0
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_50
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 11
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    jal isEven
    addi $sp, $sp, 4
    move $t0, $v0
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_51
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_51
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 8
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 2
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 3
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    mul $t0, $t0, $t1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_52
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 0
    sw $t0, -40($fp)
    li $t0, 0
    sw $t0, -8($fp)
loop_start_23:
    lw $t0, -40($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -8($fp)
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    lw $t0, 0($t1)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    sw $t0, -40($fp)
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    sw $t0, -8($fp)
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 8
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, loop_end_24
    j loop_start_23
loop_end_24:
    la $t0, str_53
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -40($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_54
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 0
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 0
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    lw $t0, 0($t1)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 100
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    la $t1, g_A
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    li $t0, 0
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    lw $t0, 0($t1)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_55
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
exit_main_2:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    li $v0, 10
    syscall
