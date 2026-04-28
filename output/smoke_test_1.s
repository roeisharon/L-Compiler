.data
string_access_violation: .asciiz "Access Violation"
string_illegal_div_by_0: .asciiz "Illegal Division By Zero"
string_invalid_ptr_dref: .asciiz "Invalid Pointer Dereference"
.text
__div_by_zero:
	la $a0,string_illegal_div_by_0
	li $v0,4
	syscall
	li $v0,10
	syscall
.text
j main
f:
	addiu $sp,$sp,-8
	sw $fp,4($sp)
	sw $ra,0($sp)
	move $fp,$sp
	lw $t0,12($fp)
	lw $t1,8($fp)
	add $t2,$t0,$t1
.data
global_z_8: .word 0
.text
	sw $t2,global_z_8
	lw $t3,global_z_8
	move $v0,$t3
	j f_epilogue
f_epilogue:
	lw $ra,0($fp)
	lw $fp,4($fp)
	addiu $sp,$sp,8
	jr $ra
main:
	addiu $sp,$sp,-8
	sw $fp,4($sp)
	sw $ra,0($sp)
	move $fp,$sp
	li $t4,2
	li $t5,1
	addiu $sp,$sp,-4
	sw $t5,0($sp)
	addiu $sp,$sp,-4
	sw $t4,0($sp)
	jal f
	addiu $sp,$sp,8
	move $t6,$v0
.data
global_z_10: .word 0
.text
	sw $t6,global_z_10
	lw $t7,global_z_10
	move $a0,$t7
	li $v0,1
	syscall
	li $a0,32
	li $v0,11
	syscall
	j main_epilogue
main_epilogue:
	lw $ra,0($fp)
	lw $fp,4($fp)
	addiu $sp,$sp,8
	li $v0,10
	syscall
