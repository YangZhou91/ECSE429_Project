package ca.mcgill.sel.ram.controller;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.sel.commons.emf.util.AdapterFactoryRegistry;
import ca.mcgill.sel.commons.emf.util.ResourceManager;
import ca.mcgill.sel.ram.Aspect;
import ca.mcgill.sel.ram.Interaction;
import ca.mcgill.sel.ram.Lifeline;
import ca.mcgill.sel.ram.MessageView;
import ca.mcgill.sel.ram.RamPackage;
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
        MessageView messageView = (MessageView) aspect.getMessageViews().get(0);
        Interaction owner = messageView.getSpecification();
        Lifeline self = owner.getLifelines().get(0);
        int lifelineCount = owner.getLifelines().size();

        // Creates a new lifeline of itself
        controller.createLifeline(owner, self.getRepresents(), 0, 0);
        assertEquals(lifelineCount + 1, owner.getLifelines().size());
    }

    /**
     * Test method for {@link MessageViewController#createLifelineWithMessage
     * (Interaction, TypedElement, float, float, Lifeline, FragmentContainer, Operation, int)}.
     */
    @Test
    public void testCreateLifelineWithMessage() {
        fail("Not yet implemented");
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
