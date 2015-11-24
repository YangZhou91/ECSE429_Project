package ca.mcgill.sel.ram.controller;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.sel.commons.emf.util.AdapterFactoryRegistry;
import ca.mcgill.sel.commons.emf.util.EMFEditUtil;
import ca.mcgill.sel.commons.emf.util.ResourceManager;
import ca.mcgill.sel.ram.Aspect;
import ca.mcgill.sel.ram.Classifier;
import ca.mcgill.sel.ram.CombinedFragment;
import ca.mcgill.sel.ram.FragmentContainer;
import ca.mcgill.sel.ram.Interaction;
import ca.mcgill.sel.ram.LayoutElement;
import ca.mcgill.sel.ram.Lifeline;
import ca.mcgill.sel.ram.Message;
import ca.mcgill.sel.ram.MessageOccurrenceSpecification;
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

    /**
     * Test method for {@link MessageViewController#MessageViewController()}.
     */
    @Test
    public void testMessageViewController() {
        // Nothing to test here
    }

    /**
     * One test case can cover all paths (there is only 1) <br>
     * Test method for {@link MessageViewController#createLifeline(Interaction, TypedElement, float, float)}.
     */
    @Test
    public void testCreateLifeline() {
        // 68-69-70-72
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething = classA.getOperations().get(0);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething);
        Interaction owner = messageView.getSpecification();
        
        TypedElement classB = classA.getAssociationEnds().get(0);
        int lifelineCount = owner.getLifelines().size();
        
        controller.createLifeline(owner, classB, 0, 0);
        
        // lifeline count is size - 1
        Lifeline newLifeline = owner.getLifelines().get(lifelineCount);
        EMap<EObject, LayoutElement> myMap = aspect.getLayout().getContainers().get(messageView);
        LayoutElement lifelineLayout = myMap.get(newLifeline);

        assertEquals(lifelineCount + 1, owner.getLifelines().size());
        assertEquals(0f, lifelineLayout.getX(), Float.MIN_VALUE);
        assertEquals(0f, lifelineLayout.getY(), Float.MIN_VALUE);
    }

    /**
     * Need to include the following transition for the paths for all-uses (or all-p-uses-some-c?) <br>
     * 93-101-102, 93-101-104 (var = container) <br>
     * 102-107, 104-107 (var = previousFragment) <br>
     * <strike>107-112-113, 107-112-116 (var = initialMessage)</strike> 
     * TA said initialMessage == null is not possible anymore <br>
     * 93-*-121-123, 93-*-121-144 (var = represents) <br>
     * 123-*-133-134, 123-*-133-137 (var = staticOperation) <br>
     * 93-*-159-160, 93-*-159-170 (var = owner, container) <br>
     * 148-*-159-163-164, 148-*-159-163-166 (var = newlifeline) <br>
     * <br>
     * Need 3 test cases <br>
     * 1) 93-101-102-<strike>107-112-116</strike>
     *  -121-123-133-134-148-159-160-163-164 <br>
     * 2) 93-101-104-<strike>107-112-113-116</strike>
     *  -121-123-133-137-148-159-160-163-166 <br>
     * 3) 93-101-102-<strike>107-112-116</strike>
     *  -121-144-159-170 <br>
     * <br>
     *  Test path 1: 93-101-102-121-123-133-134-148-159-160-163-164 <br>
     * <br>
     * Test method for {@link MessageViewController#createLifelineWithMessage
     * (Interaction, TypedElement, float, float, Lifeline, FragmentContainer, Operation, int)}.
     */
    @Test
    public void testCreateLifelineWithMessage01() {
        // container.getFragments.size > 0
        // represents is Reference and represents.container is null (is metaclass)
        // staticOperation == true
        // owner != container 
        // newlifeline not covered by combined fragments
        
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething6 = classA.getOperations().get(5);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething6);
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
        
        // Get newly added lifeline
        Lifeline newLifeline = owner.getLifelines().get(previousLifelineCount);
        EMap<EObject, LayoutElement> myMap = aspect.getLayout().getContainers().get(messageView);
        LayoutElement lifelineLayout = myMap.get(newLifeline);
        
        assertEquals(previousLifelineCount + 1, owner.getLifelines().size());
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
        assertEquals(0f, lifelineLayout.getX(), Float.MIN_VALUE);
        assertEquals(0f, lifelineLayout.getY(), Float.MIN_VALUE);
    }
    
    /**
     * Test path 2: 93-101-104-121-123-133-137-148-159-160-163-166.
     * @see #testCreateLifelineWithMessage01
     */
    @Test
    public void testCreateLifelineWithMessage02() {
        // container.getFragments.size == 0
        // represents is Reference and represents.container is null (is metaclass)
        // staticOperation == false
        // owner != container 
        // newlifeline covered by combined fragments
        
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething6 = classA.getOperations().get(5);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething6);
        Interaction owner = messageView.getSpecification();
        
        Classifier classC = aspect.getStructuralView().getClasses().get(2);
        Operation create = classC.getOperations().get(0);        
        Reference classCType = RamFactory.eINSTANCE.createReference();
        classCType.setType(classC);
        
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        
        // TODO Find a way to make newlifeline covered by combined fragments
        // Impossible perhaps?
        
        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(2);
        FragmentContainer container = cf.getOperands().get(0);
        
        int addAtIndex = 0;
        
        int previousLifelineCount = owner.getLifelines().size();
        int previousMessageCount = owner.getMessages().size();
        
        controller.createLifelineWithMessage(owner, classCType, 0, 0, lifelineFrom, 
                container, create, addAtIndex);
        
        // Get newly added lifeline
        Lifeline newLifeline = owner.getLifelines().get(previousLifelineCount);
        EMap<EObject, LayoutElement> myMap = aspect.getLayout().getContainers().get(messageView);
        LayoutElement lifelineLayout = myMap.get(newLifeline);
        
        assertEquals(previousLifelineCount + 1, owner.getLifelines().size());
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
        assertEquals(0f, lifelineLayout.getX(), Float.MIN_VALUE);
        assertEquals(0f, lifelineLayout.getY(), Float.MIN_VALUE);
    }
    
    /**
     * Test path 3: 93-101-102-121-144-159-170.
     */
    @Test
    public void testCreateLifelineWithMessage03() {
        // container.getFragments.size > 0
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
        
        // Get newly added lifeline
        Lifeline newLifeline = owner.getLifelines().get(previousLifelineCount);
        EMap<EObject, LayoutElement> myMap = aspect.getLayout().getContainers().get(messageView);
        LayoutElement lifelineLayout = myMap.get(newLifeline);
        
        assertEquals(previousLifelineCount + 1, owner.getLifelines().size());
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
        assertEquals(0f, lifelineLayout.getX(), Float.MIN_VALUE);
        assertEquals(0f, lifelineLayout.getY(), Float.MIN_VALUE);
    }

    /**
     * Single test case is enough to cover all paths (only 1 path) <br>
     * Test method for {@link MessageViewController#moveLifeline(Lifeline, float, float)}.
     */
    @Test
    public void testMoveLifeline() {
        MessageView messageView = (MessageView) aspect.getMessageViews().get(0);
        Lifeline lifeline = messageView.getSpecification().getLifelines().get(0);
        EMap<EObject, LayoutElement> myMap = aspect.getLayout().getContainers().get(messageView);

        LayoutElement lifelineLayout = myMap.get(lifeline);
        float x = lifelineLayout.getX();
        float y = lifelineLayout.getY();
        controller.moveLifeline(lifeline, x * x, x * x);
        
        assertEquals(x * x, lifelineLayout.getX(), Float.MIN_VALUE);
        assertEquals(y * y, lifelineLayout.getY(), Float.MIN_VALUE);
    }

    /**
     * Need to include following transitions for all-p-uses <br>
     * 240-253-262 (var container, owner) <br>
     * 240-253-254 (var container, owner) <br>
     * 240-253-254-257-258 (var lifelineTo) <br>
     * 240-253-254-257-260 (var lifelineTo) <br>
     * <br>
     * 3 test cases for create message <br>
     * 1) 240-253-262<br>
     * 2) 240-253-254-257-258<br>
     * 3) 240-253-254-257-260<br>
     * <br>
     * Test path 1: 240-253-262<br>
     * <br>
     * Test method for {@link MessageViewController#createMessage
     * (Interaction, Lifeline, Lifeline, FragmentContainer, Operation, int)}.
     */
    @Test
    public void testCreateMessage01() {
        // owner == container
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething3 = classA.getOperations().get(2);
        Operation doSomething = classA.getOperations().get(0);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething3);
        Interaction owner = messageView.getSpecification();
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        Lifeline lifelineTo = lifelineFrom;
        
        // Add before last message
        int addAtIndex = owner.getFragments().size() - 2;
        
        int previousMessageCount = owner.getMessages().size();
        
        controller.createMessage(owner, lifelineFrom, lifelineTo, owner, doSomething, addAtIndex);
        
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
    }
    
    /**
     * Test path 2: 240-253-254-257-258.
     * @see #testCreateLifelineWithMessage01()
     */
    @Test
    public void testCreateMessage02() {
        // owner != container
        // lifelineTo not covered by combined fragments
        
        // owner == container
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething3 = classA.getOperations().get(2);
        
        Classifier classC = aspect.getStructuralView().getClasses().get(2);
        Operation getRandom = classC.getOperations().get(1);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething3);
        Interaction owner = messageView.getSpecification();
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        Lifeline lifelineTo = owner.getLifelines().get(1);

        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(3);
        FragmentContainer container = cf.getOperands().get(0);
        int addAtIndex = 0;
        
        int previousMessageCount = owner.getMessages().size();
        
        controller.createMessage(owner, lifelineFrom, lifelineTo, container, getRandom, addAtIndex);
        
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
    }
    
    /**
     * Test path 3: 240-253-254-257-260.
     * @see #testCreateMessage01
     */
    @Test
    public void testCreateMessage03() {
        // owner != container
        // lifelineTo covered by combined fragments
        // owner == container
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething3 = classA.getOperations().get(2);   
        Operation doSomething = classA.getOperations().get(0);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething3);
        Interaction owner = messageView.getSpecification();
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        Lifeline lifelineTo = lifelineFrom;

        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(3);
        FragmentContainer container = cf.getOperands().get(0);
        int addAtIndex = 0;

        int previousMessageCount = owner.getMessages().size();
        
        controller.createMessage(owner, lifelineFrom, lifelineTo, container, doSomething, addAtIndex);
        
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
    }

    /**
     * One path is enough to cover all case
     * <br>
     * Test method for {@link MessageViewController#createReplyMessage
     * (Interaction, Lifeline, Lifeline, FragmentContainer, Operation, int)}.
     */
    @Test
    public void testCreateReplyMessage() {
        Classifier classC = aspect.getStructuralView().getClasses().get(2);
        Operation replyInt = classC.getOperations().get(3);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, replyInt);
        Interaction owner = messageView.getSpecification();
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        Lifeline lifelineTo = null;
        
        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(1);
        FragmentContainer container = cf.getOperands().get(0);
        
        int addAtIndex = 0;
        
        int previousMessageCount = owner.getMessages().size();
        
        controller.createReplyMessage(owner, lifelineFrom, lifelineTo, container, replyInt, addAtIndex);
        
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
    }

    /**
     * One path to cover all through method (not sure if using boolean variable is p-use or c-use, 
     * although it's handled in extra test 4)
     * <br>
     * Test method for {@link MessageViewController#removeMessages
     * (Interaction, FragmentContainer, MessageOccurrenceSpecification)}.
     */
    @Test
    public void testRemoveMessages01() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething5 = classA.getOperations().get(4);   
                
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething5);
        Interaction owner = messageView.getSpecification();
        MessageOccurrenceSpecification sendEvent = (MessageOccurrenceSpecification) owner.getFragments().get(1);

        int previousMessageCount = owner.getMessages().size();
        
        // Delete sendEvent (which should delete 3 messages in total)
        controller.removeMessages(owner, owner, sendEvent);
        
        assertEquals(previousMessageCount - 3, owner.getMessages().size());
    }

    /**
     * Need 2 paths alternating removeFromCovered<br>
     * Use removeFromCovered = true <br>
     * Test method for {@link MessageViewController#appendRemoveEmptyLifelinesCommand
     * (EditingDomain, CompoundCommand, Interaction, List, boolean)}.
     */
    @Test
    public void testAppendRemoveEmptyLifelinesCommand01() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething7 = classA.getOperations().get(6);   
                
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething7);
        Interaction owner = messageView.getSpecification();
        EditingDomain domain = EMFEditUtil.getEditingDomain(owner);
        
        CombinedFragment cf1 = (CombinedFragment) owner.getFragments().get(1);
        CombinedFragment cf2 = (CombinedFragment) owner.getFragments().get(2);
        CombinedFragment cf3 = (CombinedFragment) owner.getFragments().get(3);
        
        CompoundCommand compoundCommand = new CompoundCommand();
        compoundCommand.append(RemoveCommand.create(domain, cf3));
        compoundCommand.append(RemoveCommand.create(domain, owner.getMessages().get(0)));
        compoundCommand.append(RemoveCommand.create(domain, owner.getLifelines().get(2).getCoveredBy()));
        
        List<CombinedFragment> combinedFragments = new ArrayList<CombinedFragment>();
        combinedFragments.add(cf1);
        combinedFragments.add(cf2);
        
        int numCommand = compoundCommand.getCommandList().size();
        
        MessageViewController.appendRemoveEmptyLifelinesCommand(domain, compoundCommand, 
                owner, combinedFragments, true);        
        
        // Only one lifeline should be removed (classC)
        // The method is supposed to add 2 command for every deleted lifelines
        // (if removeFromCovered == true, we would expect an
        // additional numLifelineDeleted * numCombinedFragments commands)
        assertEquals(numCommand + 2 + (1 * combinedFragments.size()), 
                compoundCommand.getCommandList().size());
    }
    
    /**
     * Use removeFromCovered = false. <br>
     * @see #testAppendRemoveEmptyLifelinesCommand01()
     */
    @Test
    public void testAppendRemoveEmptyLifelinesCommand02() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething7 = classA.getOperations().get(6);   
                
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething7);
        Interaction owner = messageView.getSpecification();
        EditingDomain domain = EMFEditUtil.getEditingDomain(owner);
        
        CombinedFragment cf1 = (CombinedFragment) owner.getFragments().get(1);
        CombinedFragment cf2 = (CombinedFragment) owner.getFragments().get(2);
        CombinedFragment cf3 = (CombinedFragment) owner.getFragments().get(3);
        
        CompoundCommand compoundCommand = new CompoundCommand();
        compoundCommand.append(RemoveCommand.create(domain, cf3));
        compoundCommand.append(RemoveCommand.create(domain, owner.getMessages().get(0)));
        compoundCommand.append(RemoveCommand.create(domain, owner.getLifelines().get(2).getCoveredBy()));
        
        List<CombinedFragment> combinedFragments = new ArrayList<CombinedFragment>();
        combinedFragments.add(cf1);
        combinedFragments.add(cf2);
        
        int numCommand = compoundCommand.getCommandList().size();
        
        MessageViewController.appendRemoveEmptyLifelinesCommand(domain, compoundCommand, 
                owner, combinedFragments, false);
        
        // Only one lifeline should be removed (classC)
        // The method is supposed to add 2 command for every deleted lifelines
        // (if removeFromCovered == true, we would expect an
        // additional numLifeline * numCombinedFragments commands)
        assertEquals(numCommand + 2, compoundCommand.getCommandList().size());
    }

    /**
     * One should be enough if we use the loop in the path
     * <br>
     * Test method for {@link MessageViewController#getDeletedLifelines(CompoundCommand)}.
     */
    @Test
    public void testGetDeletedLifelines() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething2 = classA.getOperations().get(3);   
                
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething2);
        Interaction owner = messageView.getSpecification();
        EditingDomain domain = EMFEditUtil.getEditingDomain(owner);
        
        List<Lifeline> lifelines = owner.getLifelines();
        List<Message> messages = owner.getMessages();
        
        CompoundCommand compoundCommand = new CompoundCommand();
        CompoundCommand nestedCC = new CompoundCommand();
        
        nestedCC.append(AddCommand.create(domain, owner, 
                RamPackage.Literals.INTERACTION__LIFELINES, RamFactory.eINSTANCE.createLifeline()));
        
        compoundCommand.append(RemoveCommand.create(domain, owner, 
                RamPackage.Literals.INTERACTION__LIFELINES, lifelines));
        compoundCommand.append(RemoveCommand.create(domain, owner, 
                RamPackage.Literals.INTERACTION__MESSAGES, messages));
        compoundCommand.append(nestedCC);
        
        int numLifelines = lifelines.size();
        
        assertEquals(numLifelines, MessageViewController.getDeletedLifelines(compoundCommand).size());
    }

    /**
     * Test method for {@link MessageViewController#createRemoveMessagesCommand
     * (EditingDomain, Interaction, FragmentContainer, MessageOccurrenceSpecification)}.
     */
    @Test
    public void testCreateRemoveMessagesCommand01() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething8 = classA.getOperations().get(7);   
                
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething8);
        Interaction owner = messageView.getSpecification();
        EditingDomain domain = EMFEditUtil.getEditingDomain(owner);
        
        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(1);
        FragmentContainer container = cf.getOperands().get(0);
        MessageOccurrenceSpecification sendEvent = (MessageOccurrenceSpecification) container.getFragments().get(0);
        
        CompoundCommand cc = MessageViewController.createRemoveMessagesCommand(domain, owner, 
                container, sendEvent);
        
        assertEquals(9, cc.getCommandList().size());
    }
    
    /**
     * Test for remove a reply message.<br>
     * @see #testCreateRemoveMessagesCommand01()
     */
    @Test
    public void testCreateRemoveMessagesCommand02() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething8 = classA.getOperations().get(7);   
                
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething8);
        Interaction owner = messageView.getSpecification();
        EditingDomain domain = EMFEditUtil.getEditingDomain(owner);
        MessageOccurrenceSpecification sendEvent = (MessageOccurrenceSpecification) owner.getFragments().get(
                owner.getFragments().size() - 1);
        
        CompoundCommand cc = MessageViewController.createRemoveMessagesCommand(domain, owner, 
                owner, sendEvent);
        
        // 2 commands + 1 command for the message occurrence
        assertEquals(2 + 1, cc.getCommandList().size());
    }
    
    /**
     * Test case: when currentFragment !instanceof MessageOccurence.
     * @see #testCreateRemoveMessagesCommand01()
     */
    @Test
    public void testCreateRemoveMessagesCommand03() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething6 = classA.getOperations().get(5);
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething6);
        
        Interaction owner = messageView.getSpecification();
        EditingDomain domain = EMFEditUtil.getEditingDomain(owner);
        MessageOccurrenceSpecification sendEvent = (MessageOccurrenceSpecification) owner.getFragments().get(0);
        
        CompoundCommand cc = MessageViewController.createRemoveMessagesCommand(domain, owner, 
                owner, sendEvent);
        
        // 2 commands + 1 command for the message occurrence
        assertEquals(2 + 1, cc.getCommandList().size());
    }
    
    /**
     * Test case: messageEnd.getMessage().getReceiveEvent() != messageEnd <br>
     * structuralFeature != null && structuralFeature.eContainer() == initialMessage.
     * @see #testCreateRemoveMessagesCommand01()
     */
    @Test
    public void testCreateRemoveMessgesCommand04() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething4 = classA.getOperations().get(3);
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething4);
        
        Interaction owner = messageView.getSpecification();
        EditingDomain domain = EMFEditUtil.getEditingDomain(owner);
        MessageOccurrenceSpecification sendEvent = (MessageOccurrenceSpecification) owner.getFragments().get(1);
        
        CompoundCommand cc = MessageViewController.createRemoveMessagesCommand(domain, owner, 
                owner, sendEvent);
        
        // 2 commands + 1 for initialMessage, + 2 message occurrence on both sides
        assertEquals(2 + 1 + 2, cc.getCommandList().size());
    }

    
    /************************************************
     * EXTRA TEST CASES FOR ACHIEVING HIGHER COVERAGE
     ************************************************/
    
    /**
     * Extra test 1: Creating message with more parameters.
     */
    @Test
    public void testCreateMessage04() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething5 = classA.getOperations().get(4);   

        Classifier classB = aspect.getStructuralView().getClasses().get(1);
        Operation setValue = classB.getOperations().get(0);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething5);
        Interaction owner = messageView.getSpecification();
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        Lifeline lifelineTo = owner.getLifelines().get(1);

        int addAtIndex = 1;

        int previousMessageCount = owner.getMessages().size();
        
        controller.createMessage(owner, lifelineFrom, lifelineTo, owner, setValue, addAtIndex);
        
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
    }
    
    /**
     * Extra test 2: Creating a destroy message.
     */
    @Test
    public void testCreateMessage05() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething9 = classA.getOperations().get(8);   

        Classifier classD = aspect.getStructuralView().getClasses().get(3);
        Operation destroy = classD.getOperations().get(1);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething9);
        Interaction owner = messageView.getSpecification();
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        Lifeline lifelineTo = owner.getLifelines().get(1);

        int addAtIndex = 2;

        int previousMessageCount = owner.getMessages().size();
        
        controller.createMessage(owner, lifelineFrom, lifelineTo, owner, destroy, addAtIndex);
        
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
    }
    
    /**
     * Extra test 3: Creating a message with undefined messageView.
     */
    @Test
    public void testCreateMessage06() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething9 = classA.getOperations().get(8);   

        Classifier classD = aspect.getStructuralView().getClasses().get(3);
        Operation getState = classD.getOperations().get(2);
        
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething9);
        Interaction owner = messageView.getSpecification();
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        Lifeline lifelineTo = owner.getLifelines().get(1);

        int addAtIndex = 2;

        int previousMessageCount = owner.getMessages().size();
        
        controller.createMessage(owner, lifelineFrom, lifelineTo, owner, getState, addAtIndex);
        
        // adds a send and reply 
        assertEquals(previousMessageCount + 2, owner.getMessages().size());
    }
    
    /**
     * Extra test 4: Removing messages with empty combined fragments.
     * @see #testRemoveMessages01()
     */
    @Test
    public void testRemoveMessages02() {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething10 = classA.getOperations().get(9);   
                
        MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething10);
        Interaction owner = messageView.getSpecification();
        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(1);
        FragmentContainer container = cf.getOperands().get(0);
        MessageOccurrenceSpecification sendEvent = (MessageOccurrenceSpecification) container.getFragments().get(0);
        
        int previousMessageCount = owner.getMessages().size();
        
        // Delete sendEvent (which should delete 3 messages in total)
        controller.removeMessages(owner, container, sendEvent);
        
//        assertEquals(previousMessageCount - 3, owner.getMessages().size());
    }
}
