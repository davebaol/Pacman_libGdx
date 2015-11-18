package com.ychstudio.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.ychstudio.components.MovementComponent;
import com.ychstudio.components.PlayerComponent;
import com.ychstudio.components.StateComponent;
import com.ychstudio.gamesys.GameManager;

public class PlayerSystem extends IteratingSystem {

    private ComponentMapper<PlayerComponent> playerM = ComponentMapper.getFor(PlayerComponent.class);
    private ComponentMapper<MovementComponent> movementM = ComponentMapper.getFor(MovementComponent.class);
    private ComponentMapper<StateComponent> stateM = ComponentMapper.getFor(StateComponent.class);

    private Vector2 tmpV1 = new Vector2();
    private Vector2 tmpV2 = new Vector2();
    private boolean canMove;

    private enum MoveDir {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public PlayerSystem() {
        super(Family.all(PlayerComponent.class, MovementComponent.class, StateComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StateComponent state = stateM.get(entity);
        MovementComponent movement = movementM.get(entity);
        Body body = movement.body;

        if (Gdx.input.isKeyPressed(Input.Keys.D) && checkMovable(body, MoveDir.RIGHT)) {
            body.applyLinearImpulse(tmpV1.set(movement.speed, 0).scl(body.getMass()), body.getWorldCenter(), true);
            state.setState(PlayerComponent.MOVE_RIGHT);

        } else if (Gdx.input.isKeyPressed(Input.Keys.A) && checkMovable(body, MoveDir.LEFT)) {
            body.applyLinearImpulse(tmpV1.set(-movement.speed, 0).scl(body.getMass()), body.getWorldCenter(), true);
            state.setState(PlayerComponent.MOVE_LEFT);

        } else if (Gdx.input.isKeyPressed(Input.Keys.W) && checkMovable(body, MoveDir.UP)) {
            body.applyLinearImpulse(tmpV1.set(0, movement.speed).scl(body.getMass()), body.getWorldCenter(), true);
            state.setState(PlayerComponent.MOVE_UP);

        } else if (Gdx.input.isKeyPressed(Input.Keys.S) && checkMovable(body, MoveDir.DOWN)) {
            body.applyLinearImpulse(tmpV1.set(0, -movement.speed).scl(body.getMass()), body.getWorldCenter(), true);
            state.setState(PlayerComponent.MOVE_DOWN);

        }

        if (body.getLinearVelocity().len2() < 0.1f) {

            switch (state.getState()) {
                case PlayerComponent.MOVE_UP:
                case PlayerComponent.IDLE_UP:
                    state.setState(PlayerComponent.IDLE_UP);
                    break;
                case PlayerComponent.MOVE_DOWN:
                case PlayerComponent.IDLE_DOWN:
                    state.setState(PlayerComponent.IDLE_DOWN);
                    break;
                case PlayerComponent.MOVE_LEFT:
                case PlayerComponent.IDLE_LEFT:
                    state.setState(PlayerComponent.IDLE_LEFT);
                    break;
                case PlayerComponent.MOVE_RIGHT:
                case PlayerComponent.IDLE_RIGHT:
                    state.setState(PlayerComponent.IDLE_RIGHT);
                    break;
                default:
                    break;
            }
        }

    }

    private boolean checkMovable(Body body, MoveDir dir) {
        canMove = true;
        World world = body.getWorld();

        RayCastCallback rayCastCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture.getFilterData().categoryBits == GameManager.WALL_BIT || fixture.getFilterData().categoryBits == GameManager.GATE_BIT) {
                    canMove = false;
                    return 0;
                }
                return 0;
            }
        };

        for (int i = 0; i < 2; i++) {
            tmpV1.set(body.getPosition());
            switch (dir) {
                case UP:
                    tmpV2.set(body.getPosition().x - (i - 0.5f) * 0.2f, body.getPosition().y + 0.6f);
                    break;
                case DOWN:
                    tmpV2.set(body.getPosition().x - (i - 0.5f) * 0.2f, body.getPosition().y - 0.6f);
                    break;
                case LEFT:
                    tmpV2.set(body.getPosition().x - 0.6f, body.getPosition().y - (i - 0.5f) * 0.2f);
                    break;
                case RIGHT:
                    tmpV2.set(body.getPosition().x + 0.6f, body.getPosition().y - (i - 0.5f) * 0.2f);
                    break;
                default:
                    break;
            }

            world.rayCast(rayCastCallback, tmpV1, tmpV2);
        }
        
        return canMove;
    }
}