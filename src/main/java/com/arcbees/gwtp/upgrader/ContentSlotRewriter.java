package com.arcbees.gwtp.upgrader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.ModifierVisitorAdapter;

public class ContentSlotRewriter extends AbstractReWriter {
	private final static Logger LOGGER = Logger.getGlobal();

	private final static Set<String> slotMethodNames = getSlotMethodNames();

	private final Set<String> allPresenters;

	private final Map<String, Set<String>> slotNames = new HashMap<>();

	public ContentSlotRewriter(Set<String> allPresenters) {
		this.allPresenters = allPresenters;
	}

	private static Set<String> getSlotMethodNames() {
		Set<String> smNames = new HashSet<>();
		smNames.add("addToSlot");
		smNames.add("clearSlot");
		smNames.add("removeFromSlot");
		smNames.add("setInSlot");
		return smNames;
	}

	private void processNode(Node node) {
		if (node instanceof MethodCallExpr) {
			MethodCallExpr mExpr = (MethodCallExpr) node;
			if (slotMethodNames.contains(mExpr.getName())) {
				if (mExpr.getArgs() != null) {
					String enclosingClassName = getEnclosingClassName();
					Expression slotName = mExpr.getArgs().get(0);
					boolean slotNameExists = false;
					if (slotName instanceof NameExpr) {
						slotNameExists = doesSlotNameExist(enclosingClassName, (NameExpr) slotName);
					} else if (slotName instanceof FieldAccessExpr) {
						FieldAccessExpr fieldSn = (FieldAccessExpr) slotName;
						if (fieldSn.getScope() != null) {
							slotNameExists = doesSlotNameExist(getFullyQualifiedName(((NameExpr) fieldSn.getScope()).getName()), ((FieldAccessExpr) slotName).getFieldExpr());
						}

					}

					if (!slotNameExists) {
						if (mExpr.getScope() == null) {
							if (allPresenters.contains(enclosingClassName)) {
								if (slotName instanceof NameExpr) {
									addSlotName(enclosingClassName, (NameExpr) slotName);
								} else if (slotName instanceof FieldAccessExpr) {
									FieldAccessExpr fieldSn = (FieldAccessExpr) slotName;
									if (fieldSn.getScope() != null) {
										addSlotName(getFullyQualifiedName(((NameExpr) fieldSn.getScope()).getName()), ((FieldAccessExpr) slotName).getFieldExpr());
									}

								}

							}
						} else {
							Expression scope = mExpr.getScope();
							while (scope != null && scope instanceof MethodCallExpr) {
								scope = ((MethodCallExpr) scope).getScope();
							}
							if (!(scope instanceof SuperExpr)) {
								if (askUser(mExpr.toString(), slotName.toString())) {
									if (slotName instanceof NameExpr) {
										addSlotName(enclosingClassName, (NameExpr) slotName);
									} else if (slotName instanceof FieldAccessExpr) {
										FieldAccessExpr fieldSn = (FieldAccessExpr) slotName;
										if (fieldSn.getScope() != null) {
											addSlotName(getFullyQualifiedName(((NameExpr) fieldSn.getScope()).getName()), ((FieldAccessExpr) slotName).getFieldExpr());
										}

									}
								}
							}
						}
					}
				}

			}

		} else if (node instanceof AnnotationExpr) {
			AnnotationExpr anno = (AnnotationExpr) node;
			if (isFullyQualifiedMatch(anno.getName(), "com.gwtplatform.mvp.client.annotations.ContentSlot")) {
				rewriteContentSlot(anno.getParentNode());
			}
		}
		for (Node child : node.getChildrenNodes()) {
			processNode(child);
		}

	}

	private boolean askUser(String statement, String slotName) {
	
		return JOptionPane.showConfirmDialog(null, "In: " + statement + "\n\n Is " + slotName + " a slot?", "In: " + statement + "\n\n Is " + slotName + " a slot?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
		
	}

	private boolean doesSlotNameExist(Set<String> fullyQualifiedNames, NameExpr fieldExpr) {
		for (String fqName : fullyQualifiedNames) {
			if (doesSlotNameExist(fqName, fieldExpr)) {
				return true;
			}
		}
		return false;
	}

	private boolean doesSlotNameExist(String enclosingClassName, NameExpr slotName) {
		return (slotNames.containsKey(enclosingClassName) && slotNames.get(enclosingClassName).contains(slotName.getName()));

	}

	private void addSlotName(String fqName, NameExpr slotName) {
		if (!slotNames.containsKey(fqName)) {
			slotNames.put(fqName, new HashSet<String>());
		}
		slotNames.get(fqName).add(slotName.getName());
	}

	private void addSlotName(Set<String> fullyQualifiedNames, NameExpr slotName) {
		for (String fqName : fullyQualifiedNames) {
			addSlotName(fqName, slotName);
		}

	}

	


	private boolean isFullyQualifiedMatch(NameExpr nameExpr, String fullyQualified) {
		String name = nameExpr.toString();
		return getFullyQualifiedName(name).contains(fullyQualified);
	}

	private void rewriteContentSlot(Node contentSlotNode) {
		if (contentSlotNode instanceof FieldDeclaration) {
			FieldDeclaration fDec = (FieldDeclaration) contentSlotNode;
			Iterator<AnnotationExpr> it = fDec.getAnnotations().iterator();
			while (it.hasNext()) {
				if (isFullyQualifiedMatch(it.next().getName(), "com.gwtplatform.mvp.client.annotations.ContentSlot")) {
					it.remove();
					markChanged();
				}
			}
			if (hasChanged()) {
				removeImport("com.gwtplatform.mvp.client.annotations.ContentSlot");
				addImports("com.gwtplatform.mvp.client.presenter.slots.ContentSlot", "com.gwtplatform.mvp.client.Presenter");
				Type t = fDec.getType();
				ReferenceType nt = ASTHelper.createReferenceType("ContentSlot<Presenter<?,?>>", 0);
				if (t instanceof ReferenceType) {
					ReferenceType rt = (ReferenceType) t;
					rt.setType(nt);
				}
				for (VariableDeclarator v : fDec.getVariables()) {
					Expression scope = null;
					if (v.getInit() instanceof ObjectCreationExpr) {
						scope = ((ObjectCreationExpr) v.getInit()).getScope();
					}
					v.setInit(new ObjectCreationExpr(scope, new ClassOrInterfaceType("ContentSlot<Presenter<?,?>>"), null));
				}
			}
		}

	}



	

	@Override
	void processCompilationUnit() {
		processNode(getCompilationUnit());
	}

	public Map<String, Set<String>> getSlotNames() {
		return slotNames;
	}

}
