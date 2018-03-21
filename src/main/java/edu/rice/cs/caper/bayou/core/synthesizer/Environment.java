/*
Copyright 2017 Rice University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.rice.cs.caper.bayou.core.synthesizer;

import org.apache.commons.lang3.ClassUtils;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class Environment {

    private final Stack<Scope> scopes;
    Set<Class> imports;
    final AST ast;
    final Synthesizer.Mode mode;

    public AST ast() {
        return ast;
    }

    public Environment(AST ast, List<Variable> variables, Synthesizer.Mode mode) {
        this.ast = ast;
        this.scopes = new Stack<>();
        this.scopes.push(new Scope(variables));
        this.mode = mode;
        imports = new HashSet<>();
    }

    /**
     * Adds a variable with the given type (and default properties) to the current scope
     *
     * @param type variable type, from which a variable name will be derived
     * @return a TypedExpression with a simple name (variable name) and variable type
     */
    public TypedExpression addVariable(Type type) {
        VariableProperties properties = new VariableProperties().setJoin(true); // default properties
        Variable var = scopes.peek().addVariable(type, properties);
        imports.add(var.getType().C());
        return new TypedExpression(var.createASTNode(ast), var.getType());
    }

    /**
     * Adds a variable with the given type and properties to the current scope
     *
     * @param type       variable type, from which a variable name will be derived
     * @param properties variable properties
     * @return a TypedExpression with a simple name (variable name) and variable type
     */
    public TypedExpression addVariable(Type type, VariableProperties properties) {
        Variable var = scopes.peek().addVariable(type, properties);
        imports.add(var.getType().C());
        return new TypedExpression(var.createASTNode(ast), var.getType());
    }

    /**
     * Adds the given variable, whose properties must already be set, to the current scope
     *
     * @param var the variable to be added
     * @return a TypedExpression with a simple name (variable name) and variable type
     */
    public TypedExpression addVariable(Variable var) {
        scopes.peek().addVariable(var);
        imports.add(var.getType().C());
        return new TypedExpression(var.createASTNode(ast), var.getType());
    }

    public Type searchType() {
        Enumerator enumerator = new Enumerator(ast, this, mode);
        return enumerator.searchType();
    }

    public TypedExpression search(SearchTarget target) throws SynthesisException {
        Enumerator enumerator = new Enumerator(ast, this, mode);
        TypedExpression tExpr = enumerator.search(target);
        if (tExpr == null)
            throw new SynthesisException(SynthesisException.TypeNotFoundDuringSearch);
        return tExpr;
    }

    public Scope getScope() {
        return scopes.peek();
    }

    public void pushScope() {
        Scope newScope = new Scope(scopes.peek());
        scopes.push(newScope);
    }

    public Scope popScope() {
        return scopes.pop();
    }

    public static Class getClass(String name) {
        try {
            return ClassUtils.getClass(Synthesizer.classLoader, name);
        } catch (ClassNotFoundException e) {
            throw new SynthesisException(SynthesisException.ClassNotFoundInLoader);
        }
    }

    public void addImport(Class c) {
        imports.add(c);
    }

}
