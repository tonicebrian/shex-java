package fr.univLille.cristal.shex.schema.FOL.parsing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import fr.univLille.cristal.shex.schema.Label;
import fr.univLille.cristal.shex.schema.FOL.formula.And;
import fr.univLille.cristal.shex.schema.FOL.formula.Exists;
import fr.univLille.cristal.shex.schema.FOL.formula.Forall;
import fr.univLille.cristal.shex.schema.FOL.formula.Formula;
import fr.univLille.cristal.shex.schema.FOL.formula.Implication;
import fr.univLille.cristal.shex.schema.FOL.formula.Not;
import fr.univLille.cristal.shex.schema.FOL.formula.OpDiff;
import fr.univLille.cristal.shex.schema.FOL.formula.OpEqual;
import fr.univLille.cristal.shex.schema.FOL.formula.OpEqualInf;
import fr.univLille.cristal.shex.schema.FOL.formula.OpEqualSup;
import fr.univLille.cristal.shex.schema.FOL.formula.OpInf;
import fr.univLille.cristal.shex.schema.FOL.formula.OpSup;
import fr.univLille.cristal.shex.schema.FOL.formula.Operator;
import fr.univLille.cristal.shex.schema.FOL.formula.Or;
import fr.univLille.cristal.shex.schema.FOL.formula.Quantifier;
import fr.univLille.cristal.shex.schema.FOL.formula.Sentence;
import fr.univLille.cristal.shex.schema.FOL.formula.ShapePredicate;
import fr.univLille.cristal.shex.schema.FOL.formula.TriplePredicate;
import fr.univLille.cristal.shex.schema.FOL.formula.Variable;
import fr.univLille.cristal.shex.schema.FOL.parsing.FOLParser.FormulaContext;
import fr.univLille.cristal.shex.schema.FOL.parsing.FOLParser.SentenceContext;


public class FOLVisitorImpl extends FOLBaseVisitor<Object> {
	private final static ValueFactory rdfFactory = SimpleValueFactory.getInstance();
	private Map<String,String> prefixes;
	
	public ArrayList<Formula> visitFormulas(String formulas) throws IOException{
		InputStream is = new ByteArrayInputStream(formulas.getBytes());
		Reader isr = new InputStreamReader(is,Charset.defaultCharset().name());
		ANTLRInputStream inputStream = new ANTLRInputStream(isr);
		FOLLexer folLexer = new FOLLexer(inputStream);
		CommonTokenStream commonTokenStream = new CommonTokenStream(folLexer);
		FOLParser folParser = new FOLParser(commonTokenStream);
		 FOLParser.FormulasContext context = folParser.formulas();
		 return (ArrayList<Formula>) context.accept(this);
	}
	
	
	//----------------------------
	// Formula
	//----------------------------
	@Override 
	public ArrayList<Formula> visitFormulas(FOLParser.FormulasContext ctx) {
		ArrayList<Formula> formulas = new ArrayList<Formula>();
		for (FormulaContext child: ctx.formula())
			formulas.add((Formula) child.accept(this));
		return formulas; 
	}
	
	@Override 
	public Formula visitFormula(FOLParser.FormulaContext ctx) { 
		ArrayList<Quantifier> quantifiers = (ArrayList<Quantifier>) ctx.quantifiers().accept(this);
		Sentence sent = (Sentence) ctx.sentence().accept(this);
		return new Formula(quantifiers,sent); 
	}
	
	//----------------------------
	// Quantifiers
	//----------------------------	
	@Override 
	public ArrayList<Quantifier> visitQuantifiers(FOLParser.QuantifiersContext ctx) { 
		ArrayList<Quantifier> quantifiers = new ArrayList<Quantifier>();
		for (FOLParser.QuantifierContext child: ctx.quantifier())
			quantifiers.add((Quantifier) child.accept(this));
		return quantifiers; 
	}
	
	public Quantifier visitForallQuantifier(FOLParser.ForallQuantifierContext ctx) { 
		return new Forall(new Variable(ctx.VARIABLE().getText())); 
	}
	
	@Override 
	public Quantifier visitExistsQuantifier(FOLParser.ExistsQuantifierContext ctx) { 
		return new Exists(new Variable(ctx.VARIABLE().getText())); 
	}

	//----------------------------
	// Sentence logic
	//----------------------------
	@Override 
	public Sentence visitSentenceParenthesis(FOLParser.SentenceParenthesisContext ctx) { 
		return (Sentence) ctx.sentence().accept(this); 
	}

	@Override 
	public Sentence visitSentenceOr(FOLParser.SentenceOrContext ctx) { 
		ArrayList<Sentence> subs = new ArrayList<Sentence>();
		for (SentenceContext sub:ctx.sentence())
			subs.add((Sentence) sub.accept(this));
		return new Or(subs); 
	}
	

	@Override 
	public Sentence visitSentenceNot(FOLParser.SentenceNotContext ctx) { 
		return new Not((Sentence) ctx.sentence().accept(this));
	}
	

	@Override
	public Sentence visitSentenceAnd(FOLParser.SentenceAndContext ctx) { 
		ArrayList<Sentence> subs = new ArrayList<Sentence>();
		for (SentenceContext sub:ctx.sentence())
			subs.add((Sentence) sub.accept(this));
		return new And(subs); 
	}
	
	@Override 
	public Sentence visitSentenceImplication(FOLParser.SentenceImplicationContext ctx) { 
		return new Implication((Sentence) ctx.sentence(0).accept(this),(Sentence) ctx.sentence(1).accept(this)); 
		}

	
	//----------------------------
	// Sentence operators
	//----------------------------
	
	@Override
	public Operator visitOperatorEqual(FOLParser.OperatorEqualContext ctx) { 
		return new OpEqual(new Variable(ctx.VARIABLE(0).getText()),new Variable(ctx.VARIABLE(1).getText()));
	}

	@Override 
	public Operator visitOperatorEqualInf(FOLParser.OperatorEqualInfContext ctx) { 
		return new OpEqualInf(new Variable(ctx.VARIABLE(0).getText()),new Variable(ctx.VARIABLE(1).getText()));
	}

	@Override 
	public Operator visitOperatorEqualSup(FOLParser.OperatorEqualSupContext ctx) { 
		return new OpEqualSup(new Variable(ctx.VARIABLE(0).getText()),new Variable(ctx.VARIABLE(1).getText()));
	}

	@Override 
	public Operator visitOperatorInf(FOLParser.OperatorInfContext ctx) { 
		return new OpInf(new Variable(ctx.VARIABLE(0).getText()),new Variable(ctx.VARIABLE(1).getText()));
	}

	@Override 
	public Operator visitOperatorSup(FOLParser.OperatorSupContext ctx) { 
		return new OpSup(new Variable(ctx.VARIABLE(0).getText()),new Variable(ctx.VARIABLE(1).getText()));
	}
	
	@Override
	public Operator visitOperatorDiff(FOLParser.OperatorDiffContext ctx) { 
		return new OpDiff(new Variable(ctx.VARIABLE(0).getText()),new Variable(ctx.VARIABLE(1).getText()));
	}
	
	//----------------------------
	// predicate
	//----------------------------
	
	@Override 
	public ShapePredicate visitShapePredicate(FOLParser.ShapePredicateContext ctx) { 
		return new ShapePredicate((Label) ctx.label().accept(this), new Variable(ctx.VARIABLE().getText())); 
	}
	
	@Override 
	public TriplePredicate visitTriplePredicate(FOLParser.TriplePredicateContext ctx) {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		for (TerminalNode var:ctx.VARIABLE())
			variables.add(new Variable(var.getText()));
		return new TriplePredicate((Label) ctx.label().accept(this),variables.get(0),variables.get(1));
	}

	//----------------------------
	//base
	//----------------------------
	
//	@Override 
//	public Variable visitVariable(FOLParser.VariableContext ctx) {
//		return new Variable(ctx.VARIABLE().getText());
//	}

	@Override 
	public Label visitLabel(FOLParser.LabelContext ctx) { 
		if (ctx.iri()!= null)
			return new Label((IRI) ctx.iri().accept(this));
		else
			return new Label((BNode) ctx.blankNode().accept(this));
	}
	
	@Override
	public Object visitBlankNode(FOLParser.BlankNodeContext ctx) { 
		return rdfFactory.createBNode(ctx.BLANK_NODE_LABEL().getText().substring(2));
	}
	
	@Override 
	public Object visitIri(FOLParser.IriContext ctx) {
		TerminalNode iri = ctx.IRIREF();
		if (iri != null) {
			String iris = iri.getText();
			iris = iris.substring(1,iris.length()-1);
			return rdfFactory.createIRI(iris);
		}
		return ctx.prefixedName().accept(this); 
	}
	
	@Override 
	public Object visitPrefixedName(FOLParser.PrefixedNameContext ctx) {
		if (ctx.PNAME_NS()!=null)
			return rdfFactory.createIRI(prefixes.get(ctx.PNAME_NS().getText()));

		String prefix = ctx.PNAME_LN().getText().split(":")[0]+":";
		String value = (ctx.PNAME_LN().getText().replaceAll(prefix, prefixes.get(prefix)));
		return rdfFactory.createIRI(value); 
	}
}
