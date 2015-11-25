package ca.mcgill.sel.ram.ui.views.message.handler.impl;

/**
 * Integration Test(Top-Bottom) from MessageViewHandler to MessageViewController
 * Yang Zhou
 * ID: 260401719
 */
import static org.junit.Assert.*;

import java.awt.event.MouseEvent;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mt4j.components.MTComponent;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeEvent;
import org.mt4j.sceneManagement.ISceneChangeListener;
import org.mt4j.sceneManagement.SceneChangeEvent;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;

import ca.mcgill.sel.commons.emf.util.AdapterFactoryRegistry;
import ca.mcgill.sel.commons.emf.util.ResourceManager;
import ca.mcgill.sel.ram.Aspect;
import ca.mcgill.sel.ram.Classifier;
import ca.mcgill.sel.ram.CombinedFragment;
import ca.mcgill.sel.ram.FragmentContainer;
import ca.mcgill.sel.ram.Interaction;
import ca.mcgill.sel.ram.LayoutElement;
import ca.mcgill.sel.ram.Lifeline;
import ca.mcgill.sel.ram.MessageView;
import ca.mcgill.sel.ram.Operation;
import ca.mcgill.sel.ram.RamFactory;
import ca.mcgill.sel.ram.RamPackage;
import ca.mcgill.sel.ram.Reference;
import ca.mcgill.sel.ram.provider.RamItemProviderAdapterFactory;
import ca.mcgill.sel.ram.ui.RamApp;
import ca.mcgill.sel.ram.ui.scenes.DisplayAspectScene;
import ca.mcgill.sel.ram.ui.views.message.LifelineView;
import ca.mcgill.sel.ram.ui.views.message.MessageViewView;
import ca.mcgill.sel.ram.ui.views.message.handler.MessageViewHandlerFactory;
import ca.mcgill.sel.ram.util.RAMModelUtil;
import ca.mcgill.sel.ram.util.RamResourceFactoryImpl;

public class MessageViewHandlerIntegrationTest {

    private static Object waiter = new Object();
    private static volatile boolean mayProceed;

    private static MessageViewHandler handler;

    private Aspect aspect;
    private String aspectLocation = "../ca.mcgill.sel.ram.gui/models/ecse429_test_models/TestModel01.ram";

    /**
     * Set up the resources and GUI.
     * 
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
        handler = (MessageViewHandler) MessageViewHandlerFactory.INSTANCE.getMessageViewHandler();
    }

    // /**
    // * @throws java.lang.Exception if something went wrong.
    // */
    // @AfterClass
    // public static void tearDownAfterClass() throws Exception {
    // }

    /**
     * Loads aspect before test.
     * 
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
     * Unloads aspect after test case.
     * 
     * @throws java.lang.Exception if something went wrong.
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
     * Test case1: Gesture is ended and reaches a lifeline that has no fragment. <br>
     * createMessages by MessageViewController
     * 
     * @throws InterruptedException
     */
    @Test
    public void test01() throws InterruptedException {
        RamApp app = RamApp.getApplication();

        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething = classA.getOperations().get(0);
        Operation doSomething3 = classA.getOperations().get(2);

        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething3);
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();

        // stub
        Interaction owner = messageView.getSpecification();
        Lifeline lifelineFrom = owner.getLifelines().get(0);
        Lifeline lifelineTo = lifelineFrom;

        // Add before last message
        int addAtIndex = owner.getFragments().size() - 2;

        int previousMessageCount = owner.getMessages().size();

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
        InputCursor cursor = new InputCursor();
        cursor.getEvents().add(new MTFingerInputEvt(null, 160, 180, 0, cursor));
        cursor.getEvents().add(new MTFingerInputEvt(null, 470, 180, 0, cursor));
        final UnistrokeEvent event = new UnistrokeEvent(null, MTGestureEvent.GESTURE_ENDED,
                mvv, null, null, cursor);

        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();

        // Process the event
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.processUnistrokeEvent(event);
                appNotify();
            }
        });
        unitTestWait();

        // A selector should have popped up
        assertEquals(previousChildCount + 1, topLayer.getChildCount());

        // Try to click one of the options (click on the destroy method)
        click(app, 480, 210);
        waitNextRenderLoop(app);

        // Pop up have disappeared, and message should have increased by 1
        assertEquals(previousMessageCount + 1, messageView.getSpecification().getMessages().size());

        // Integration to MessageViewController for createMessages
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
    }

    /**
     * Set up for CreatFramentTest
     **/
    @Test
    public void testHandleCreateFragment01() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething1 = classA.getOperations().get(0);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething1);
        final Interaction owner = messageView.getSpecification();
        int previousFragmentCount = owner.getFragments().size();
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

        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();

        final MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        final LifelineView llv = mvv.getLifelineView(owner.getLifelines().get(0));

        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.handleCreateFragment(mvv, llv, new Vector3D(160, 180), owner);
                appNotify();
            }
        });

        unitTestWait();

        // A pop-up should appear
        assertEquals(previousChildCount + 1, topLayer.getChildCount());

        // Now try to click create statement
        click(app, 150, 180);
        waitNextRenderLoop(app);

        // Pop should disappear and fragment count increased by 1
        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousFragmentCount + 1, owner.getFragments().size());
    }

    /**
     * Create fragment and click create reply message.
     * Integrate test reply message
     * 
     * @throws InterruptedException if interrupted
     * @see #testHandleCreateFragment01()
     */
    @Test
    public void test02() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething3 = classA.getOperations().get(2);
        // final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething3);
        // Set up for Test CreateRepley Message
        Classifier classC = aspect.getStructuralView().getClasses().get(2);
        Operation replyInt = classC.getOperations().get(3);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, replyInt);
        Interaction owner = messageView.getSpecification();
        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(1);
        final FragmentContainer container = cf.getOperands().get(0);
        int previousMessageCount = owner.getMessages().size();
        int previousFragmentCount = container.getFragments().size();
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();

        Lifeline lifelineFrom = owner.getLifelines().get(0);
        Lifeline lifelineTo = null;

        int addAtIndex = 0;

        // Changes the scene
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                aspectScene.showMessageView(messageView);
                appNotify();
            }
        });
        unitTestWait();

        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();

        final MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        final LifelineView llv = mvv.getLifelineView(owner.getLifelines().get(0));

        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.handleCreateFragment(mvv, llv, new Vector3D(160, 230), container);
                appNotify();
            }
        });

        unitTestWait();

        // A pop-up should appear
        assertEquals(previousChildCount + 1, topLayer.getChildCount());

        // Now try to click create reply message
        click(app, 160, 310);
        waitNextRenderLoop(app);

        // Pop should disappear and fragment count increased by 1
        // And message count by 1
        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousFragmentCount + 1, container.getFragments().size());
        assertEquals(previousMessageCount + 1, owner.getMessages().size());

        // Integration test check CreateMessage
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
    }

    /**
     * Gesture is ended and does not reaches a lifeline. <br>
     * Integration test createLifelinewithMessage
     * 
     * @throws InterruptedException if interrupted
     * @see #testProcessUnistrokeEvent01()
     */
    @Test
    public void test03() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething6 = classA.getOperations().get(5);

        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething6);
        Interaction owner = messageView.getSpecification();

        Classifier classC = aspect.getStructuralView().getClasses().get(2);
        Reference classCType = RamFactory.eINSTANCE.createReference();
        classCType.setType(classC);

        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(1);
        FragmentContainer container = cf.getOperands().get(0);

        int previousLifelineCount = owner.getLifelines().size();
        int previousMessageCount = owner.getMessages().size();
        // final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething1);
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();
        // int previousLifelineCount = messageView.getSpecification().getLifelines().size();

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
        InputCursor cursor = new InputCursor();
        cursor.getEvents().add(new MTFingerInputEvt(null, 160, 180, 0, cursor));
        cursor.getEvents().add(new MTFingerInputEvt(null, 260, 180, 0, cursor));
        final UnistrokeEvent event = new UnistrokeEvent(null, MTGestureEvent.GESTURE_ENDED,
                mvv, null, null, cursor);

        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();

        // Process the event
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.processUnistrokeEvent(event);
                appNotify();
            }
        });
        unitTestWait();

        // A selector should have popped up
        assertEquals(previousChildCount + 1, topLayer.getChildCount());

        // Try to click one of the options
        click(app, 275, 250);

        // Clicks the create method (which should increase lifeline count and pop-up disappears)
        click(app, 140, 120);

        waitNextRenderLoop(app);

        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousLifelineCount + 1, messageView.getSpecification().getLifelines().size());

        // Get newly added lifeline
        Lifeline newLifeline = owner.getLifelines().get(previousLifelineCount);
        EMap<EObject, LayoutElement> myMap = aspect.getLayout().getContainers().get(messageView);
        LayoutElement lifelineLayout = myMap.get(newLifeline);

        assertEquals(previousLifelineCount + 1, owner.getLifelines().size());
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
    }

    /**
     * Makes the Test suite wait for the UI updated.
     * 
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

    /**
     * Simulates a click.
     * 
     * @param app The app.
     * @param x The x position.
     * @param y The y position.
     * @throws InterruptedException if interrupted
     */
    private static void click(RamApp app, int x, int y) throws InterruptedException {
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
    }

    /**
     * Waits for the next render loop.
     * 
     * @param app The app.
     * @throws InterruptedException if interrupted.
     */
    private static void waitNextRenderLoop(RamApp app) throws InterruptedException {
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                appNotify();
            }
        });

        // This waits for the next render loop
        unitTestWait();
    }

}
