package com.santiagolovisotto.proteinaligner;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.santiagolovisotto.proteinaligner");
    }

    @Test
    void alignmentNoDependeDeWeb() {
        noClasses().that().resideInAPackage("..alignment..")
                .should().dependOnClassesThat().resideInAPackage("..web..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void pdbNoDependeDeAlignmentNiWeb() {
        noClasses().that().resideInAPackage("..pdb..")
                .should().dependOnClassesThat().resideInAnyPackage("..alignment..", "..web..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void biojavaSoloEnAdaptador() {
        noClasses().that().resideOutsideOfPackage("..alignment.biojava..")
                .should().dependOnClassesThat().resideInAPackage("org.biojava..")
                .allowEmptyShould(true)
                .check(classes);
    }
}