package ca.mcgill.sel.ram.controller;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.sel.commons.emf.util.AdapterFactoryRegistry;
import ca.mcgill.sel.commons.emf.util.EMFModelUtil;
import ca.mcgill.sel.commons.emf.util.ResourceManager;
import ca.mcgill.sel.ram.Aspect;
import ca.mcgill.sel.ram.Classifier;
import ca.mcgill.sel.ram.CombinedFragment;
import ca.mcgill.sel.ram.FragmentContainer;
import ca.mcgill.sel.ram.Interaction;
import ca.mcgill.sel.ram.InteractionFragment;
import ca.mcgill.sel.ram.Lifeline;
import ca.mcgill.sel.ram.MessageView;
import ca.mcgill.sel.ram.Operation;
import ca.mcgill.sel.ram.RamFactory;
import ca.mcgill.sel.ram.RamPackage;
import ca.mcgill.sel.ram.Reference;
import ca.mcgill.sel.ram.TypedElement;
import ca.mcgill.sel.ram.provider.RamItemProviderAdapterFactory;
import ca.mcgill.sel.ram.util.RAMModelUtil;
import ca.mcgill.sel.ram.util.RamResourceFactoryImpl;

/**
 * Test case for {@link MessageViewController}.
 * @author Wayne 260532614
 *
 */
public class MessageViewControllerTest {
    
    private static MessageViewController controller;
    
    private Aspect aspect;
    private String aspectLocation = "../ca.mcgill.sel.ram.gui/models/ecse429_test_models/TestModel01.ram";
    
    /**
     * Sets up various classes.
     * @throws java.lang.Exception if something went wrong
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Initialize ResourceManager.
        ResourceManager.initialize();
        // Initialize packages.
        RamPackage.eINSTANCE.eClass();

        // Register resource factories.
        ResourceManager.registerExtensionFactory("ram", new RamResourceFactoryImpl());

        // Initialize adapter factories.
        AdapterFactoryRegistry.INSTANCE.addAdapterFactory(RamItemProviderAdapterFactory.class);
        
        // Get the controller
        controller = ControllerFactory.INSTANCE.getMessageViewController();
    }

    /**
     * @throws java.lang.Exception if something went wrong
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * Loads up the aspect to test.
     * @throws java.lang.Exception if something went wrong
     */
    @Before
    public void setUp() throws Exception {
        aspect = (Aspect) ResourceManager.loadModel(aspectLocation);
    }

    /**
     * Unloads the aspect to test.
     * @throws java.lang.Exception if something went wrong
     */
    @After
    public void tearDown() throws Exception {
        ResourceManager.unloadResource(aspect.eResource());
    }

//    /**
//     * Test method for {@link MessageViewController#MessageViewController()}.
//     */
//    @Test
//    public void testMessageViewController() {
//        fail("Not yet implemented");
//    }

    /**
     * Test method for {@link MessageViewController#createLifeline(Interaction, TypedElement, float, float)}.
     */
    @Test
    public void testCreateLifeline() {
        // One test case can cover all du-paths
        // 68-69-70-72
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething = classA.getOperations().get(0);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething);
        Interaction owner = messageView.getSpecification();
        
        TypedElement classB = classA.getAssociationEnds().get(0);
        int lifelineCount = owner.getLifelines().size();

        controller.createLifeline(owner, classB, 0, 0);
        assertEquals(lifelineCount + 1, owner.getLifelines().size());
    }

    /**
     * Need to include the following transition for the paths for all-uses (or all-p-uses-some-c?) <br>
     * 93-101-102, 93-101-104 (var = container) <br>
     * 102-107, 104-107 (var = previousFragment) <br>
     * 107-112-113, 107-112-116 (var = initialMessage) <br>
     * 93-*-121-123, 93-*-121-144 (var = represents) <br>
     * 123-*-133-134, 123-*-133-137 (var = staticOperation) <br>
     * 93-*-159-160, 93-*-159-170 (var = owner, container) <br>
     * 148-*-159-163-164, 148-*-159-163-166 (var = newlifeline) <br>
     * <br>
     * Need 3 test cases <br>
     * 1) 93-101-102-107-112-116-121-123-133-134-148-159-160-163-164 <br>
     * 2) 93-101-104-107-112-113-116-121-123-133-137-148-159-160-163-166 <br>
     * 3) 93-101-102-107-112-116-121-144-159-170 <br>
     * <br>
     *  Test path 1: 93-101-102-107-112-116-121-123-133-134-148-159-160-163-164 <br>
     * <br>
     * Test method for {@link MessageViewController#createLifelineWithMessage
     * (Interaction, TypedElement, float, float, Lifeline, FragmentContainer, Operation, int)}.
     */
    @Test
    public void testCreateLifelineWithMessage01() {
        // container.getFragments.size > 0
        // initialMessage != null
        // represents is Reference and represents.container is null (is metaclass)
        // staticOperation == true
        // owner != container 
        // newlifeline not covered by combined fragments
        
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething2 = classA.getOperations().get(1);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething2);
        Interaction owner = messageView.getSpecification();
        
        Classifier classC = aspect.getStructuralView().getClasses().get(2);
        Operation getRandom = classC.getOperations().get(1);        
        Reference classCType = RamFactory.eINSTANCE.createReference();
        classCType.setType(classC);
        
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        
        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(1);
        FragmentContainer container = cf.getOperands().get(0);
        int previousFragIndex = container.getFragments().size() - 1;
        
        int previousLifelineCount = owner.getLifelines().size();
        int previousMessageCount = owner.getMessages().size();
        
        controller.createLifelineWithMessage(owner, classCType, 0, 0, lifelineFrom, 
                container, getRandom, previousFragIndex);
        
        assertEquals(owner.getLifelines().size(), previousLifelineCount + 1);
        assertEquals(owner.getMessages().size(), previousMessageCount + 1);
    }
    
    /**
     * Test path 2: 93-101-104-107-112-113-116-121-123-133-137-148-159-160-163-166.
     * @see #testCreateLifelineWithMessage01
     */
    @Test
    public void testCreateLifelineWithMessage02() {
        // container.getFragments.size == 0
        // initialMessage == null
        // represents is Reference and represents.container is null (is metaclass)
        // staticOperation == false
        // owner != container 
        // newlifeline covered by combined fragments
        
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething2 = classA.getOperations().get(1);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething2);
        Interaction owner = messageView.getSpecification();
        
        Classifier classC = aspect.getStructuralView().getClasses().get(2);
        Operation create = classC.getOperations().get(0);        
        Reference classCType = RamFactory.eINSTANCE.createReference();
        classCType.setType(classC);
        
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        
        // TODO Find a fragment combination such that initial message is null
        // TODO Find a way to make newlifeline covered by combined fragments
        
        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(5);
        FragmentContainer container = cf.getOperands().get(0);
        
        int addAtIndex = 0;
        
        int previousLifelineCount = owner.getLifelines().size();
        int previousMessageCount = owner.getMessages().size();
        
        controller.createLifelineWithMessage(owner, classCType, 0, 0, lifelineFrom, 
                container, create, addAtIndex);
        
        assertEquals(owner.getLifelines().size(), previousLifelineCount + 1);
        assertEquals(owner.getMessages().size(), previousMessageCount + 1);
    }
    
    /**
     * Test path 3: 93-101-102-107-112-116-121-144-159-170.
     */
    @Test
    public void testCreateLifelineWithMessage03() {
        // container.getFragments.size > 0
        // initialMessage != null
        // represents is Reference and represents.container is not null (not metaclass)
        // owner == container 
        
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething = classA.getOperations().get(0);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething);
        Interaction owner = messageView.getSpecification();
        
        Classifier classB = classA.getAssociationEnds().get(0).getClassifier();
        TypedElement classBType = classA.getAssociationEnds().get(0);
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        int addAtIndex = 0;
        Operation signature = classB.getOperations().get(0);
        
        int previousLifelineCount = owner.getLifelines().size();
        int previousMessageCount = owner.getMessages().size();
        
        controller.createLifelineWithMessage(owner, classBType, 0, 0, lifelineFrom, owner, signature, addAtIndex);
        
        assertEquals(owner.getLifelines().size(), previousLifelineCount + 1);
        assertEquals(owner.getMessages().size(), previousMessageCount + 1);
    }

    /**
     * Test method for {@link MessageViewController#moveLifeline(Lifeline, float, float)}.
     */
    @Test
    public void testMoveLifeline() {
//        MessageView messageView = (MessageView) aspect.getMessageViews().get(0);
//        Lifeline lifeline = messageView.getSpecification().getLifelines().get(0);
//        
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link MessageViewController#createMessage
     * (Interaction, Lifeline, Lifeline, FragmentContainer, Operation, int)}.
     */
    @Test
    public void testCreateMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link MessageViewController#createReplyMessage
     * (Interaction, Lifeline, Lifeline, FragmentContainer, Operation, int)}.
     */
    @Test
    public void testCreateReplyMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link MessageViewController#removeMessages
     * (Interaction, FragmentContainer, MessageOccurrenceSpecification)}.
     */
    @Test
    public void testRemoveMessages() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link MessageViewController#appendRemoveEmptyLifelinesCommand
     * (EditingDomain, CompoundCommand, Interaction, List, boolean)}.
     */
    @Test
    public void testAppendRemoveEmptyLifelinesCommand() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link MessageViewController#getDeletedLifelines(CompoundCommand)}.
     */
    @Test
    public void testGetDeletedLifelines() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link MessageViewController#createRemoveMessagesCommand
     * (EditingDomain, Interaction, FragmentContainer, MessageOccurrenceSpecification)}.
     */
    @Test
    public void testCreateRemoveMessagesCommand() {
        fail("Not yet implemented");
    }

}
