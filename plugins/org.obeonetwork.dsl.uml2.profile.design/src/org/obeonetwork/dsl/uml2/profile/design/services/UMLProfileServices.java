/*******************************************************************************
 * Copyright (c) 2013 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.obeonetwork.dsl.uml2.profile.design.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.Extension;
import org.eclipse.uml2.uml.ExtensionEnd;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.obeonetwork.dsl.uml2.profile.design.dialogs.ExtraAssociationSelectionDialog;
import org.obeonetwork.dsl.uml2.profile.design.dialogs.ProfileVersionDialog;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * Services for UML Profile.
 * 
 * @author Mohamed-Lamine BOUKHANOUFA <a
 *         href="mailto:mohamed-lamine.boukhanoufa@obeo.fr"
 *         >mohamed-lamine.boukhanoufa@obeo.fr</a>
 */
public class UMLProfileServices {

	private static String YES = "Yes";

	private static String NO = "No";

	private static String OK = "OK";

	private static String undefineProfile = "Undefine the Profile";

	private static String base = "base_";

	/**
	 * Dialog message to define a profile.
	 * 
	 * @param rootProfile
	 *            to define
	 */
	public void defineProfileDialog(final Profile rootProfile) {
		defineProfileDialog(rootProfile, getAllSubProfiles(rootProfile));
	}

	/**
	 * Dialog message to define a profile.
	 * 
	 * @param rootProfile
	 *            to define
	 * @param allContentProfile
	 *            to define
	 */
	private void defineProfileDialog(final Profile rootProfile,
			final List<Profile> allContentProfile) {
		boolean result = false;

		final String[] buttonYes = { OK };
		final Shell activeShell = PlatformUI.getWorkbench().getDisplay()
				.getActiveShell();
		MessageDialog msgDialogYes = null;

		result = defineProfile(rootProfile, allContentProfile);

		if (result) {
			msgDialogYes = new MessageDialog(activeShell, "Define the Profile",
					null, "The profile is defined", MessageDialog.INFORMATION,
					buttonYes, 0);
			msgDialogYes.open();
		}
	}

	/**
	 * Define the profile and compute the version.
	 * 
	 * @param rootProfile
	 *            to define.
	 * @param allContentProfile
	 *            to define.
	 * @return true if defined, else false.
	 */
	public static boolean defineProfile(final Profile rootProfile,
			final List<Profile> allContentProfile) {
		boolean result = false;

		final ProfileVersionDialog versionDialog = new ProfileVersionDialog(
				PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				rootProfile);
		versionDialog.open();

		if (versionDialog.getReturnCode() == IDialogConstants.OK_ID) {
			final UMLDesignerProfileVersion uMLDesignerProfileVersion = versionDialog
					.getUMLDesignerProfileVersion();

			// Remove the oldest definitions of the profile and all sub profiles
			// then redefine
			// the whole
			undefineProfile(rootProfile);

			result = defineAndAnnotateProfile(rootProfile,
					uMLDesignerProfileVersion);


			for (Profile profile : allContentProfile) {

				result = defineAndAnnotateProfile(profile,
						uMLDesignerProfileVersion);
			}
		}
		return result;
	}

	/**
	 * Define and annotate the profile with the version
	 * profileVersionAnnotation.
	 * 
	 * @param profile
	 *            to define.
	 * @param profileVersionAnnotation
	 *            to use for the annotation of the profile.
	 * @return true if defined, else false.
	 */
	public static boolean defineAndAnnotateProfile(final Profile profile,
			final UMLDesignerProfileVersion profileVersionAnnotation) {
		boolean result = true;
		// TODO to review if we must remove the old definition of the profile
		// Remove the old definition of the root profile
		// if (rootProfile.getDefinition() != null) {
		// rootProfile.getEAnnotations().remove(rootProfile.getDefinition().eContainer());
		// }
		// Define the root profile
		if (profile.define() == null)
			result = false;
		else {
			final EPackage rootProfileDefinition = profile.getDefinition();
			// use this condition if the old definition of the profile is
			// removed
			// if (rootProfileDefinition
			// .getEAnnotation(UMLDesignerProfileVersion.UML_DESIGNER_PROFILE_EANNOTATION_SOURCE)
			// ==
			// null) {
			final EAnnotation eAnnotation = EcoreFactory.eINSTANCE
					.createEAnnotation();
			final UMLDesignerProfileVersion umlDesignerProfileVersion = new UMLDesignerProfileVersion();
			umlDesignerProfileVersion.initEAnnotationVersion(eAnnotation,
					profileVersionAnnotation);
			rootProfileDefinition.getEAnnotations().add(eAnnotation);
			// }
		}
		return result;
	}

	/**
	 * Define the profile root and all sub profile.
	 * 
	 * @param rootProfile
	 *            to define
	 * @return true if defined, else false.
	 */
	public static boolean defineAllProfiles(final Profile rootProfile) {
		return defineProfile(rootProfile, getAllSubProfiles(rootProfile));
	}

	/**
	 * Find all the sub profile of a given root profile.
	 * 
	 * @param rootProfile
	 *            the root profile
	 * @return a list all sub profile.
	 */
	public static List<Profile> getAllSubProfiles(final Profile rootProfile) {
		final List<Profile> allSubProfiles = Lists.newArrayList(Iterators
				.filter(rootProfile.eAllContents(), Profile.class));
		allSubProfiles.remove(rootProfile);

		return allSubProfiles;
	}

	/**
	 * Dialog message to undefine the profile.
	 * 
	 * @param rootProfile
	 *            to undefine
	 */
	public static void undefineProfileDialog(final Profile rootProfile) {
		final String[] buttonYesNo = { YES, NO };
		final String[] buttonYes = { OK };
		final Shell activeShell = PlatformUI.getWorkbench().getDisplay()
				.getActiveShell();
		MessageDialog msgDialogYesNo = null;
		MessageDialog msgDialogYes = null;
		if (rootProfile.getDefinition() != null) {
			msgDialogYesNo = new MessageDialog(activeShell, undefineProfile,
					null, "Would you like to undefine this profile ?",
					MessageDialog.QUESTION, buttonYesNo, 1);
			final int diagResult = msgDialogYesNo.open();
			if (diagResult == 0) {
				undefineProfile(rootProfile);
				msgDialogYes = new MessageDialog(activeShell, undefineProfile,
						null, "The profile is undefined",
						MessageDialog.INFORMATION, buttonYes, 0);
				msgDialogYes.open();
			}
		} else {
			msgDialogYes = new MessageDialog(activeShell, undefineProfile,
					null, "The profile is not defined !",
					MessageDialog.WARNING, buttonYes, 0);
			msgDialogYes.open();
		}
	}

	/**
	 * Undefine the profile.
	 * 
	 * @param rootProfile
	 *            to undefine
	 */
	public static void undefineProfile(final Profile rootProfile) {
		if (rootProfile.getDefinition() != null) {
			List<Profile> allContentProfile = getAllSubProfiles(rootProfile);
			rootProfile.getEAnnotations().remove(
					rootProfile.getDefinition().eContainer());
			for (Profile profile : allContentProfile) {
				if (profile.getDefinition() != null) {
					profile.getEAnnotations().remove(
							profile.getDefinition().eContainer());
				}
			}
		}
	}

	/**
	 * Test if is it possible to reconnect an uml Extension.
	 * 
	 * @param element
	 *            is the extension to reconnect.
	 * @param target
	 *            of the reconnection.
	 * @return true if the reconnection is possible, else false.
	 */
	public boolean canReconnectExtension(final EObject element,
			final EObject target) {
		if (element instanceof Extension) {
			final Extension extension = (Extension) element;
			if (target instanceof ElementImport) {
				final Class newPointedMetaClass = (Class) ((ElementImport) target)
						.getImportedElement();

				final NamedElement baseProperty = extension.getStereotype()
						.getMember(base + newPointedMetaClass.getName());
				if (baseProperty == null)
					return true;
				else if (baseProperty instanceof Property) {
					if (!((Property) baseProperty).getType().equals(
							newPointedMetaClass))
						return true;
				}
			}
			if (target instanceof Stereotype) {
				final NamedElement baseProperty = ((Stereotype) target)
						.getMember(base + extension.getMetaclass().getName());
				if (baseProperty == null)
					return true;
				else if (baseProperty instanceof Property) {
					if (!((Property) baseProperty).getType().equals(
							extension.getMetaclass()))
						return true;
				}
			}
		}
		return false;
	}

	public boolean canCreateExtension(final Stereotype source,
			final Stereotype target) {
		return canCreateExtension((EObject) source, (EObject) target);
	}

	public boolean canCreateExtension(final Stereotype source,
			final ElementImport target) {
		return canCreateExtension((EObject) source, (EObject) target);
	}

	/**
	 * Test if is it possible to create an extension from stereotype source to
	 * the stereotype extension.
	 * 
	 * @param source
	 *            stereotype.
	 * @param target
	 *            stereotype.
	 * @return true if the extension between source and target is possible.
	 */
	private boolean canCreateExtension(final EObject source,
			final EObject target) {
		if (source instanceof Stereotype) {
			final Stereotype stereotypeSource = (Stereotype) source;
			if (target instanceof ElementImport) {
				final ElementImport elementImport = (ElementImport) target;
				final NamedElement metaClass = elementImport
						.getImportedElement();

				// this code find all extension including inherited.
				final NamedElement baseProperty = stereotypeSource
						.getMember(base + metaClass.getName());
				if (baseProperty == null)
					return true;
				else if (baseProperty instanceof Property) {
					if (((Property) baseProperty).getType().equals(metaClass))
						return true;
				}

				// this code find local extension.
				// if (stereotypeSource.getAttribute(base +
				// metaClass.getName(), (Type)metaClass) == null
				// || stereotypeSource.getAttribute(base +
				// metaClass.getName(), (Type)metaClass)
				// .getAssociation() == null) {
				// return true;
				// }
			}
			if (target instanceof Stereotype && !source.equals(target)) {

				final Stereotype stereotypeTarget = (Stereotype) target;
				if (stereotypeSource.getGeneralization(stereotypeTarget) == null
						&& stereotypeTarget.getGeneralization(stereotypeSource) == null)
					return true;
			}
		}
		return false;
	}

	/**
	 * Get all owned attributes of the classifier.
	 * 
	 * @param classifier
	 *            to find attribute from.
	 * @return a list of found attribute.
	 */
	public List<Property> getClassifierAttributes(final Classifier classifier) {
		if (classifier instanceof Profile) {
			return null;
		}
		final List<Property> properties = new ArrayList<Property>();
		for (Property property : classifier.getAttributes()) {
			if (property.getAssociation() == null
					|| !(property.getAssociation() instanceof Extension))
				properties.add(property);
		}
		return properties;
	}

	/**
	 * Get all owned attributes of the classifier.
	 * 
	 * @param classifier
	 *            to find attribute from.
	 * @return a list of found attribute.
	 */
	public List<Property> getClassifierAttributes(final Profile classifier) {
		return null;
	}

	/**
	 * A message dialog for the creation of an extra association between the
	 * stereotype source 'stereotypeSource' and the the stereotype target
	 * 'targetElement'.
	 * 
	 * @param stereotypeSource
	 *            stereotype source.
	 * @param targetElement
	 *            stereotype target.
	 * @param profile
	 *            owner of the stereotypes.
	 */
	public void createExtraAssociationDialog(final Stereotype stereotypeSource,
			final Element targetElement, final Profile profile) {

		final ExtraAssociationSelectionDialog dialog = new ExtraAssociationSelectionDialog(
				PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				profile, stereotypeSource, targetElement, false);
		dialog.open();
		if (dialog.getResult() != null) {
			if (targetElement instanceof Stereotype) {
				final Stereotype stereotypeTarget = (Stereotype) targetElement;
					createExtraAssociation(stereotypeSource, stereotypeTarget,
							dialog.getAssociationName(), dialog.getResult(),
							profile);
			}
		}
	}

	/**
	 * Create an extra association between the stereotype source
	 * 'stereotypeSource' and the the stereotype target 'stereotypeTarget'. the
	 * result stereotype is named 'stereotypName'.
	 * 
	 * @param stereotypeSource
	 *            the stereotype source.
	 * @param stereotypeTarget
	 *            the stereotype target.
	 * @param stereotypName
	 *            the stereotype name.
	 * @param result
	 *            the metaclass selected for of the creation of the extra
	 *            association.
	 * @param profile
	 *            owner of the stereotypes.
	 * @return the new stereotype of the extra association.
	 */
	public Stereotype createExtraAssociation(final Stereotype stereotypeSource,
			final Stereotype stereotypeTarget, final String stereotypName,
			final Object[] result, final Profile profile) {
		ElementImport metaClass = null;
		Stereotype newStereotype = null;
		if (result != null && result[0] instanceof Class) {
			final Class selectedMetaclass = (Class) result[0];
			final PackageableElement importedMember = profile
					.getImportedMember(selectedMetaclass.getName());
			if (importedMember == null) {
				metaClass = profile.createMetaclassReference(selectedMetaclass);
			} else {
				metaClass = profile.getElementImport(importedMember);
			}
		}
		if (metaClass != null) {
			newStereotype = UMLFactory.eINSTANCE.createStereotype();
			newStereotype.setName(stereotypName);
			profile.getOwnedStereotypes().add(newStereotype);
			createMetaclassExtension(newStereotype, metaClass);
			createStereotypeAssociation(newStereotype, metaClass,
					stereotypeSource, stereotypeTarget);
		}
		return newStereotype;
	}

	/**
	 * Create an UML Association between the stereotypes source and target.
	 * 
	 * @param stereotypeSource
	 *            stereotype source.
	 * @param stereotypeTarget
	 *            stereotype target.
	 * @param associationName
	 *            the name of the Association.
	 * @return the created association.
	 */
	public Association createAssociation(final Stereotype stereotypeSource,
			final Stereotype stereotypeTarget, final String associationName) {
		final Association association = UMLFactory.eINSTANCE
				.createAssociation();
		association.setName(associationName);

		final Property sourceStereoProperty = UMLFactory.eINSTANCE
				.createProperty();
		sourceStereoProperty.setName(stereotypeSource.getName() + "s");
		sourceStereoProperty.setType(stereotypeSource);
		sourceStereoProperty.setLower(0);
		sourceStereoProperty.setUpper(-1);
		association.getOwnedEnds().add(sourceStereoProperty);

		final Property targetStereoProperty = UMLFactory.eINSTANCE
				.createProperty();
		targetStereoProperty.setName(stereotypeTarget.getName() + "s");
		targetStereoProperty.setType(stereotypeTarget);
		targetStereoProperty.setLower(0);
		targetStereoProperty.setUpper(-1);
		association.getOwnedEnds().add(targetStereoProperty);

		association.getNavigableOwnedEnds().add(targetStereoProperty);
		stereotypeSource.getProfile().getPackagedElements().add(association);
		return association;
	}

	/**
	 * Create an UML Association between the stereotypes source and target.
	 * 
	 * @param stereotypeSource
	 *            stereotype source.
	 * @param stereotypeTarget
	 *            stereotype target.
	 * @return the created association.
	 */
	public Association createAssociation(final Stereotype stereotypeSource,
			final Stereotype stereotypeTarget) {
		return createAssociation(stereotypeSource, stereotypeTarget,
				stereotypeSource.getName() + "To" + stereotypeTarget.getName());
	}
	/**
	 * Create an UML Extension between the stereotype source and the target
	 * element.
	 * 
	 * @param stereotype
	 *            source.
	 * @param targetElement
	 *            the target.
	 */
	public void createExtension(final Stereotype stereotype,
			final Element targetElement) {
		if (targetElement instanceof ElementImport) {
			createMetaclassExtension(stereotype, (ElementImport) targetElement);
		} else if (targetElement instanceof Stereotype) {
			createGeneralization(stereotype, (Stereotype) targetElement);
		}
	}

	/**
	 * Create an UML Generalisation between the stereotypes source and target.
	 * 
	 * @param stereotypeSource
	 *            of the generalisation.
	 * @param stereotypeTarget
	 *            of the generalisation.
	 */
	public void createGeneralization(final Stereotype stereotypeSource,
			final Stereotype stereotypeTarget) {
		if (!isGeneraleFor(stereotypeTarget, stereotypeSource)) {
			final Generalization generalization = UMLFactory.eINSTANCE
					.createGeneralization();
			generalization.setGeneral(stereotypeTarget);
			generalization.setSpecific(stereotypeSource);
			stereotypeSource.getGeneralizations().add(generalization);
		}
	}

	/**
	 * Test if the stereotype target is a generalisation of the stereotype
	 * source.
	 * 
	 * @param stereotypeTarget
	 *            of the generalisation.
	 * @param stereotypeSource
	 *            of the generalisation.
	 * @return true if the stereotype target is a generalisation of the
	 *         stereotype source.
	 */
	public boolean isGeneraleFor(final Stereotype stereotypeTarget,
			final Stereotype stereotypeSource) {
		for (Generalization generalization : stereotypeSource
				.getGeneralizations()) {
			if (generalization.getGeneral().equals(stereotypeTarget)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Create an extension from stereotype to the imported element.
	 * 
	 * @param stereotype
	 *            of the extension.
	 * @param elementImport
	 *            of the extension.
	 * @return new extension.
	 */
	public Extension createMetaclassExtension(final Stereotype stereotype,
			final ElementImport elementImport) {
		Extension extension = null;
		final PackageableElement importedElement = elementImport
				.getImportedElement();
		if (importedElement != null && importedElement instanceof Type
				&& !isExtentedBy((Type) importedElement, stereotype)) {

			final Type metaclass = (Type) importedElement;
			final Property baseMetaclass = UMLFactory.eINSTANCE
					.createProperty();
			baseMetaclass.setName(base + metaclass.getName());
			baseMetaclass.setType(metaclass);
			stereotype.getOwnedAttributes().add(baseMetaclass);

			final ExtensionEnd extensionEnd = UMLFactory.eINSTANCE
					.createExtensionEnd();
			extensionEnd.setName("extension_" + stereotype.getName());
			extensionEnd.setType(stereotype);

			extension = UMLFactory.eINSTANCE.createExtension();
			extension.getOwnedEnds().add(extensionEnd);
			extension.setName(stereotype.getName() + "Extend"
					+ metaclass.getName());
			extension.getMemberEnds().add(baseMetaclass);

			stereotype.getProfile().getPackagedElements().add(extension);
		}

		return extension;
	}

	/**
	 * Create a new {@link Stereotype} in the profile.
	 * 
	 * @param profile
	 *            the profile.
	 * @return a new stereotype.
	 */
	public Stereotype createStereotype(Profile profile) {
		Stereotype newStereotype = UMLFactory.eINSTANCE.createStereotype();
		profile.getOwnedStereotypes().add(newStereotype);
		return newStereotype;
	}

	/**
	 * Create a new {@link Stereotype} in the profile that directly or
	 * indirectly contains the package_ in parameter.
	 * 
	 * @param package_
	 *            the package_.
	 * @return a new stereotype.
	 */
	public Stereotype createStereotype(Package package_) {
		Stereotype newStereotype = UMLFactory.eINSTANCE.createStereotype();
		package_.getOwnedStereotypes().add(
					newStereotype);
		return newStereotype;
	}

	/**
	 * Create a new {@link Stereotype} in the profile that directly or
	 * indirectly contains the stereotype in parameter and create a
	 * generalization to this stereotype from the new created stereotype.
	 * 
	 * @param stereotype
	 *            the stereotype.
	 * @return a new stereotype.
	 */
	public Stereotype createStereotype(Stereotype stereotype) {
		Stereotype newStereotype = UMLFactory.eINSTANCE.createStereotype();
		if (stereotype.getOwner() instanceof Package) {
			((Package) stereotype.getOwner()).getOwnedStereotypes().add(
					newStereotype);
		}
		createGeneralization(newStereotype, stereotype);
		return newStereotype;
	}

	/**
	 * Create a new {@link Stereotype} in the profile that directly or
	 * indirectly contains the elementImport in parameter and create a extension
	 * to this elementImport from the new created stereotype.
	 * 
	 * @param elementImport
	 *            the elementImport.
	 * @return a new stereotype.
	 */
	public Stereotype createStereotype(ElementImport elementImport) {
		Stereotype newStereotype = UMLFactory.eINSTANCE.createStereotype();
		getProfileOwner(elementImport).getOwnedStereotypes().add(
					newStereotype);
		createMetaclassExtension(newStereotype, elementImport);
		return newStereotype;
	}

	/**
	 * Get the profile owner of this element.
	 * 
	 * @param umlElement
	 *            the element
	 * @return the profile
	 */
	public static Profile getProfileOwner(final Element umlElement) {

		if (umlElement.getOwner() instanceof Profile)
			return (Profile) umlElement.getOwner();
		else
			return getProfileOwner(umlElement.getOwner());
	}

	/**
	 * Create an StereotypeAssociation for the extra Association.
	 * 
	 * @param stereotype
	 *            of the extra Association
	 * @param elementImport
	 *            of the extra Association
	 * @param stereotypeSource
	 *            of the extra Association
	 * @param stereotypeTarget
	 *            of the extra Association
	 */
	public void createStereotypeAssociation(final Stereotype stereotype,
			final ElementImport elementImport,
			final Stereotype stereotypeSource, final Stereotype stereotypeTarget) {
		final PackageableElement importedElement = elementImport
				.getImportedElement();

		if (importedElement != null && importedElement instanceof Type
				&& isExtentedBy((Type) importedElement, stereotype)) {

			final Type metaclass = (Type) importedElement;
			final Class metaclassClass = (Class) importedElement;
			final ArrayList<Property> properties = UMLServices
					.getRelationShipSourceTargetPeroperties(metaclassClass);
			final Property sourceProperty = properties.get(0);
			final Property targetProperty = properties.get(1);
			metaclassClass.getAttributes();

			final Property associationSourceEnd = UMLFactory.eINSTANCE
					.createProperty();
			associationSourceEnd.setName("sourceRole_"
					+ sourceProperty.getName());
			associationSourceEnd.setType(stereotypeSource);
			stereotype.getOwnedAttributes().add(associationSourceEnd);

			final Property associationTargetEnd = UMLFactory.eINSTANCE
					.createProperty();
			associationTargetEnd.setName("targetRole_"
					+ targetProperty.getName());
			associationTargetEnd.setType(stereotypeTarget);
			stereotype.getOwnedAttributes().add(associationTargetEnd);

			final Extension extension = getExtenstion(stereotype, metaclass);

			extension.getMemberEnds().add(associationSourceEnd);
			extension.getMemberEnds().add(associationTargetEnd);
		}
	}

	/**
	 * Find the Extension of stereotype to metacalss.
	 * 
	 * @param stereotype
	 *            of the extension.
	 * @param metaclass
	 *            of the extension.
	 * @return the found extension, or null.
	 */
	public Extension getExtenstion(final Stereotype stereotype,
			final Type metaclass) {
		for (Extension extention : stereotype.getProfile().getOwnedExtensions(
				false)) {
			if (extention.getName() != null) {
				if (extention.getName().equals(
						stereotype.getName() + "Extend" + metaclass.getName())) {
					return extention;
				}
			}
		}

		return null;
	}

	/**
	 * Test if the metaclass is extended by the stereotype.
	 * 
	 * @param metaclassType
	 *            of extension
	 * @param stereotype
	 *            of extension.
	 * @return true if the metaclass is extended by the stereotype.
	 */
	public boolean isExtentedBy(final Type metaclassType,
			final Stereotype stereotype) {
		return !(stereotype.getOwnedAttribute(base + metaclassType.getName(),
				metaclassType) == null);
	}
}
