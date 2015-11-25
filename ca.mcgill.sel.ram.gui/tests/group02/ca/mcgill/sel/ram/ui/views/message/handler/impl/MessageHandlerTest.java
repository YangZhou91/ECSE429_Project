package ca.mcgill.sel.ram.ui.views.message.handler.impl;

import static org.junit.Assert.assertEquals;

import java.awt.event.MouseEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mt4j.components.MTComponent;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent;
import org.mt4j.sceneManagement.ISceneChangeListener;
import org.mt4j.sceneManagement.SceneChangeEvent;
import org.mt4j.util.math.Vector3D;

import ca.mcgill.sel.commons.emf.util.AdapterFactoryRegistry;
import ca.mcgill.sel.commons.emf.util.ResourceManager;
import ca.mcgill.sel.ram.Aspect;
import ca.mcgill.sel.ram.Classifier;
import ca.mcgill.sel.ram.Interaction;
import ca.mcgill.sel.ram.Lifeline;
import ca.mcgill.sel.ram.Message;
import ca.mcgill.sel.ram.MessageOccurrenceSpecification;
import ca.mcgill.sel.ram.MessageSort;
import ca.mcgill.sel.ram.MessageView;
import ca.mcgill.sel.ram.Operation;
import ca.mcgill.sel.ram.RamFactory;
import ca.mcgill.sel.ram.RamPackage;
import ca.mcgill.sel.ram.provider.RamItemProviderAdapterFactory;
import ca.mcgill.sel.ram.ui.RamApp;
import ca.mcgill.sel.ram.ui.events.listeners.ITapAndHoldListener;
import ca.mcgill.sel.ram.ui.scenes.DisplayAspectScene;
import ca.mcgill.sel.ram.ui.views.message.LifelineView;
import ca.mcgill.sel.ram.ui.views.message.MessageCallView;
import ca.mcgill.sel.ram.ui.views.message.MessageViewView;
import ca.mcgill.sel.ram.ui.views.message.handler.MessageViewHandlerFactory;
import ca.mcgill.sel.ram.util.RAMModelUtil;
import ca.mcgill.sel.ram.util.RamResourceFactoryImpl;

/**
 * Test suite for {@link MessageHandler}.
 * @author Yang Zhou
 *         ID: 260401719
 * @author Wayne Lee 260532614
 *
 */
public class MessageHandlerTest {

    private static Object waiter = new Object();
    private static volatile boolean mayProceed;
    
    private static ITapAndHoldListener handler;
    
    private static Aspect aspect;
    private String aspectLocation = "tests/group02/ecse429_test_models/TestModel02.ram";
    
    /**
     * Set up the Resources and GUI. 
     * @throws java.lang.Exception if something went wrong.
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
        
        RamApp.initialize(new Runnable() {
            
            @Override
            public void run() {
                appNotify();
            }
        });
        
        // Wait for RamApp to be initialized.
        unitTestWait();
        handler = MessageViewHandlerFactory.INSTANCE.getMessageHandler();
    }

//    /**
//     * Tears down after class.
//     * @throws java.lang.Exception
//     */
//    @AfterClass
//    public static void tearDownAfterClass() throws Exception {
//    }

    /**
     * Loads aspect before tests.
     * @throws java.lang.Exception if something went wrong.
     */
    @Before
    public void setUp() throws Exception {
        // Load model to use in test.
        aspect = (Aspect) ResourceManager.loadModel(aspectLocation);
        RamApp.getApplication().addSceneChangeListener(new ISceneChangeListener() {

            @Override
            public void processSceneChangeEvent(SceneChangeEvent event) {
                // Resume once the new aspect scene is loaded (switched to).
                if (event.getNewScene() instanceof DisplayAspectScene) {
                    RamApp.getApplication().removeSceneChangeListener(this);
                    appNotify();
                }
            }
        });

        RamApp.getApplication().loadAspect(aspect);

        // Wait for UI to be updated.
        unitTestWait();
    }

    /**
     * Executes after each test.
     * @throws java.lang.Exception if interrupted.
     */
    @After
    public void tearDown() throws Exception {
        // Close current aspect.
        if (aspect != null) {
            RamApp.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    RamApp.getApplication().closeAspectScene(RamApp.getActiveAspectScene());
                    ResourceManager.unloadResource(aspect.eResource());
                }
            });
        }
    }

    /**
     * Test 1: Tap and hold is incomplete. <br>
     * Test method for
     * {@link ca.mcgill.sel.ram.ui.views.message.handler.impl.MessageHandler
     * #processTapAndHoldEvent(org.mt4j.input.inputProcessors.componentProcessors.
     * tapAndHoldProcessor.TapAndHoldEvent)}
     * .
     */
    @Test
    public void testProcessTapAndHoldEvent01() {
        TapAndHoldEvent event = new TapAndHoldEvent(null, 0, null, null, false, null, 0, 0, 0);
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();

        handler.processTapAndHoldEvent(event);

        // Nothing should have happened
        assertEquals(previousChildCount, topLayer.getChildCount());
    }
    
    /**
     * Test 2: Normal message call view creation. <br>
     * @throws InterruptedException if interrupted 
     * @see #testProcessTapAndHoldEvent01
     */
    @Test
    public void testProcessTapAndHoldEvent02() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething1 = classA.getOperations().get(0);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething1);        
        Interaction interaction = messageView.getSpecification();
        final Message message = interaction.getMessages().get(2);
        
        Lifeline lifelineFrom = interaction.getLifelines().get(0);
        Lifeline lifelineTo = interaction.getLifelines().get(1);
        
        
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();
        
        // Changes the scene
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                aspectScene.showMessageView(messageView);
                appNotify();
            }
        });
        unitTestWait();
        
        MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        final LifelineView from = mvv.getLifelineView(lifelineFrom);
        final LifelineView to = mvv.getLifelineView(lifelineTo);
       
        MTComponent topLayer = aspectScene.getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        // Process tap and hold
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                MessageCallView mcv = new MessageCallView(message, from, to);
                TapAndHoldEvent event = new TapAndHoldEvent(null, 0, mcv, null, true, new Vector3D(0, 0), 0, 0, 0);
                handler.processTapAndHoldEvent(event);
                appNotify();
            }
        });
        unitTestWait();
        
        // A pop up should have appeared in the topLayer
        assertEquals(previousChildCount + 1, topLayer.getChildCount());

        // Now try to click it
        int x = 60;
        int y = 60;
        int previousMessageCount = interaction.getMessages().size();
        
        app.dispatchEvent(new MouseEvent(app, MouseEvent.MOUSE_PRESSED, 0, 
                MouseEvent.BUTTON1_MASK, x, y, x, y, 1, false, MouseEvent.BUTTON1));
        app.dispatchEvent(new MouseEvent(app, MouseEvent.MOUSE_RELEASED, 0, 
                MouseEvent.BUTTON1_MASK, x, y, x, y, 1, false, MouseEvent.BUTTON1));
        
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                appNotify();
            }
        });
        
        // This waits for the click to actually happen
        unitTestWait();
        
        // The controller should have removed the message
        assertEquals(previousMessageCount - 1, interaction.getMessages().size());
    }
    
    /**
     * Test 3: Should process == false. <br>
     * @throws InterruptedException if interrupted
     * @see #testProcessTapAndHoldEvent01
     */
    @Test
    public void testProcessTapAndHoldEvent03() throws InterruptedException {
        final Message message = RamFactory.eINSTANCE.createMessage();
        message.setSendEvent(RamFactory.eINSTANCE.createGate());
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        RamApp.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                MessageCallView mcv = new MessageCallView(message, null, null);
                TapAndHoldEvent event = new TapAndHoldEvent(null, 0, mcv, null, true, null, 0, 0, 0);
                handler.processTapAndHoldEvent(event);
                appNotify();
            }
            
        });
        unitTestWait();
        
        // Nothing should have happened
        assertEquals(previousChildCount, topLayer.getChildCount());
    }
    
    /*********************************************
     * EXTRA TEST CASES TO ACHIEVE HIGHER COVERAGE
     *********************************************/
    
    /**
     * Extra Test 1: Should process == false, sendEvent.container == interaction. <br>
     * @throws InterruptedException if interrupted
     * @see #testProcessTapAndHoldEvent01
     */
    @Test
    public void testProcessTapAndHoldEvent04() throws InterruptedException {
        final Message message = RamFactory.eINSTANCE.createMessage();
        message.setMessageSort(MessageSort.REPLY);
        message.setSendEvent(RamFactory.eINSTANCE.createMessageOccurrenceSpecification());
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        RamApp.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                MessageCallView mcv = new MessageCallView(message, null, null);
                TapAndHoldEvent event = new TapAndHoldEvent(null, 0, mcv, null, true, null, 0, 0, 0);
                handler.processTapAndHoldEvent(event);
                appNotify();
            }
            
        });
        unitTestWait();
        
        // Nothing should have happened
        assertEquals(previousChildCount, topLayer.getChildCount());
    }
    
    /**
     * Extra Test 2: Should process == false, sendEvent.container != interaction, receiveEnt != gate. <br>
     * @throws InterruptedException if interrupted
     * @see #testProcessTapAndHoldEvent01
     */
    @Test
    public void testProcessTapAndHoldEvent05() throws InterruptedException {
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething1 = classA.getOperations().get(0);
        MessageView mv = RAMModelUtil.getMessageViewFor(aspect, doSomething1);
        Interaction owner = mv.getSpecification();
        
        final Message message = owner.getMessages().get(2);
        message.setMessageSort(MessageSort.REPLY);
        message.setSendEvent(RamFactory.eINSTANCE.createMessageOccurrenceSpecification());
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        RamApp.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                MessageCallView mcv = new MessageCallView(message, null, null);
                TapAndHoldEvent event = new TapAndHoldEvent(null, 0, mcv, null, true, null, 0, 0, 0);
                handler.processTapAndHoldEvent(event);
                appNotify();
            }
            
        });
        unitTestWait();
        
        // Nothing should have happened
        assertEquals(previousChildCount, topLayer.getChildCount());
    }
    
    /**
     * Extra test 3: Normal message call view creation but with messageSort = reply. <br>
     * @throws InterruptedException if interrupted 
     * @see #testProcessTapAndHoldEvent01
     */
    @Test
    public void testProcessTapAndHoldEvent06() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething1 = classA.getOperations().get(0);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething1);        
        Interaction interaction = messageView.getSpecification();
        Message messageModel = interaction.getMessages().get(2);
        final Message message = RamFactory.eINSTANCE.createMessage();
        message.setAssignTo(messageModel.getAssignTo());
        message.setInteraction(messageModel.getInteraction());
        message.setMessageSort(MessageSort.REPLY);
        message.setReceiveEvent(interaction.getFormalGates().get(1));
        MessageOccurrenceSpecification sendEvent = RamFactory.eINSTANCE.createMessageOccurrenceSpecification();
        sendEvent.setMessage(message);
        message.setSendEvent(sendEvent);
        
        Lifeline lifelineFrom = interaction.getLifelines().get(0);
        Lifeline lifelineTo = interaction.getLifelines().get(1);
        
        
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();
        
        // Changes the scene
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                aspectScene.showMessageView(messageView);
                appNotify();
            }
        });
        unitTestWait();
        
        MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        final LifelineView from = mvv.getLifelineView(lifelineFrom);
        final LifelineView to = mvv.getLifelineView(lifelineTo);
       
        MTComponent topLayer = aspectScene.getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        // Process tap and hold
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                MessageCallView mcv = new MessageCallView(message, from, to);
                TapAndHoldEvent event = new TapAndHoldEvent(null, 0, mcv, null, true, new Vector3D(0, 0), 0, 0, 0);
                handler.processTapAndHoldEvent(event);
                appNotify();
            }
        });
        unitTestWait();
        
        // A pop up should have appeared in the topLayer
        assertEquals(previousChildCount + 1, topLayer.getChildCount());

        // Now try to click it
        int x = 60;
        int y = 60;
        int previousMessageCount = interaction.getMessages().size();
        
        app.dispatchEvent(new MouseEvent(app, MouseEvent.MOUSE_PRESSED, 0, 
                MouseEvent.BUTTON1_MASK, x, y, x, y, 1, false, MouseEvent.BUTTON1));
        app.dispatchEvent(new MouseEvent(app, MouseEvent.MOUSE_RELEASED, 0, 
                MouseEvent.BUTTON1_MASK, x, y, x, y, 1, false, MouseEvent.BUTTON1));
        
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                appNotify();
            }
        });
        
        // This waits for the click to actually happen
        unitTestWait();
        
        // The controller should not have removed the message
        assertEquals(previousMessageCount, interaction.getMessages().size());
    }
    
    /**
     * Makes the Test suite wait for the UI updated.
     * @throws InterruptedException if interrupted
     */
    private static void unitTestWait() throws InterruptedException {
        synchronized (waiter) {
            while (!mayProceed) {
                waiter.wait();
            }
            mayProceed = false;
        }
    }
    
    /**
     * Makes the app notify the test suite to continue.
     */
    private static void appNotify() {
        synchronized (waiter) {
            waiter.notify();
            mayProceed = true;
        }
    }
}
