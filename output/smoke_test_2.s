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
.data
global_z_5: .word 0
.text
.data
str_const_0: .asciiz "abcd"
.text
	la $t0, str_const_0
	sw $t0,global_z_5
.text
j main
main:
	addiu $sp,$sp,-8
	sw $fp,4($sp)
	sw $ra,0($sp)
	move $fp,$sp
	lw $t1,global_z_5
	move $a0,$t1
	li $v0,4
	syscall
	j main_epilogue
main_epilogue:
	lw $ra,0($fp)
	lw $fp,4($fp)
	addiu $sp,$sp,8
	li $v0,10
	syscall
