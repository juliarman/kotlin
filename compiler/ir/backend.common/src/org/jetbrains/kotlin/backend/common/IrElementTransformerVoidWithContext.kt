/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

open class ScopeWithIr(val scope: Scope, val irElement: IrElement)

abstract class IrElementTransformerVoidWithContext(open protected val context: CommonBackendContext) : IrElementTransformerVoid() {

    private val scopeStack = mutableListOf<ScopeWithIr>()

    protected open fun createScope(declaration: IrSymbolOwner): ScopeWithIr =
        ScopeWithIr(Scope(declaration.symbol, context.irDeclarationFactory), declaration)

    final override fun visitFile(declaration: IrFile): IrFile {
        scopeStack.push(createScope(declaration))
        val result = visitFileNew(declaration)
        scopeStack.pop()
        return result
    }

    final override fun visitClass(declaration: IrClass): IrStatement {
        scopeStack.push(createScope(declaration))
        val result = visitClassNew(declaration)
        scopeStack.pop()
        return result
    }

    final override fun visitProperty(declaration: IrProperty): IrStatement {
        scopeStack.push(createScope(declaration))
        val result = visitPropertyNew(declaration)
        scopeStack.pop()
        return result
    }

    final override fun visitField(declaration: IrField): IrStatement {
        scopeStack.push(createScope(declaration))
        val result = visitFieldNew(declaration)
        scopeStack.pop()
        return result
    }

    final override fun visitFunction(declaration: IrFunction): IrStatement {
        scopeStack.push(createScope(declaration))
        val result = visitFunctionNew(declaration)
        scopeStack.pop()
        return result
    }

    final override fun visitScript(declaration: IrScript): IrStatement {
        scopeStack.push(createScope(declaration))
        val result = visitScriptNew(declaration)
        scopeStack.pop()
        return result
    }

    protected val currentFile get() = scopeStack.lastOrNull { it.irElement is IrFile }!!.irElement as IrFile
    protected val currentScript get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrScriptSymbol }
    protected val currentClass get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrClassSymbol }
    protected val currentFunction get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrFunctionSymbol }
    protected val currentProperty get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrPropertySymbol }
    protected val currentScope get() = scopeStack.peek()
    protected val parentScope get() = if (scopeStack.size < 2) null else scopeStack[scopeStack.size - 2]
    protected val allScopes get() = scopeStack
    protected val currentDeclarationParent get() = allScopes.last { it.irElement is IrDeclarationParent }.irElement as IrDeclarationParent

    fun printScopeStack() {
        scopeStack.forEach { println(it.scope.scopeOwner) }
    }

    open fun visitFileNew(declaration: IrFile): IrFile {
        return super.visitFile(declaration)
    }

    open fun visitClassNew(declaration: IrClass): IrStatement {
        return super.visitClass(declaration)
    }

    open fun visitFunctionNew(declaration: IrFunction): IrStatement {
        return super.visitFunction(declaration)
    }

    open fun visitPropertyNew(declaration: IrProperty): IrStatement {
        return super.visitProperty(declaration)
    }

    open fun visitFieldNew(declaration: IrField): IrStatement {
        return super.visitField(declaration)
    }

    open fun visitScriptNew(declaration: IrScript): IrStatement {
        return super.visitScript(declaration)
    }
}

abstract class IrElementVisitorVoidWithContext(open protected val context: CommonBackendContext) : IrElementVisitorVoid {

    private val scopeStack = mutableListOf<ScopeWithIr>()

    protected open fun createScope(declaration: IrSymbolOwner): ScopeWithIr =
        ScopeWithIr(Scope(declaration.symbol, context.irDeclarationFactory), declaration)

    final override fun visitFile(declaration: IrFile) {
        scopeStack.push(createScope(declaration))
        visitFileNew(declaration)
        scopeStack.pop()
    }

    final override fun visitClass(declaration: IrClass) {
        scopeStack.push(createScope(declaration))
        visitClassNew(declaration)
        scopeStack.pop()
    }

    final override fun visitProperty(declaration: IrProperty) {
        scopeStack.push(createScope(declaration))
        visitPropertyNew(declaration)
        scopeStack.pop()
    }

    final override fun visitField(declaration: IrField) {
        @Suppress("DEPRECATION") val isDelegated = declaration.descriptor.isDelegated
        if (isDelegated) scopeStack.push(createScope(declaration))
        visitFieldNew(declaration)
        if (isDelegated) scopeStack.pop()
    }

    final override fun visitFunction(declaration: IrFunction) {
        scopeStack.push(createScope(declaration))
        visitFunctionNew(declaration)
        scopeStack.pop()
    }

    protected val currentFile get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrFileSymbol }
    protected val currentClass get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrClassSymbol }
    protected val currentFunction get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrFunctionSymbol }
    protected val currentProperty get() = scopeStack.lastOrNull { it.scope.scopeOwnerSymbol is IrPropertySymbol }
    protected val currentScope get() = scopeStack.peek()
    protected val parentScope get() = if (scopeStack.size < 2) null else scopeStack[scopeStack.size - 2]
    protected val allScopes get() = scopeStack

    fun printScopeStack() {
        scopeStack.forEach { println(it.scope.scopeOwner) }
    }

    open fun visitFileNew(declaration: IrFile) {
        super.visitFile(declaration)
    }

    open fun visitClassNew(declaration: IrClass) {
        super.visitClass(declaration)
    }

    open fun visitFunctionNew(declaration: IrFunction) {
        super.visitFunction(declaration)
    }

    open fun visitPropertyNew(declaration: IrProperty) {
        super.visitProperty(declaration)
    }

    open fun visitFieldNew(declaration: IrField) {
        super.visitField(declaration)
    }
}