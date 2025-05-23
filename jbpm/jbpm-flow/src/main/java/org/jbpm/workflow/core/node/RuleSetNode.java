/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jbpm.workflow.core.node;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.drools.ruleunits.api.RuleUnitData;
import org.jbpm.process.core.Context;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.AbstractContext;
import org.jbpm.process.core.impl.ContextContainerImpl;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.instance.rule.RuleType;
import org.kie.api.definition.process.Connection;
import org.kie.api.runtime.KieRuntime;
import org.kie.kogito.decision.DecisionModel;

import static org.jbpm.workflow.instance.WorkflowProcessParameters.WORKFLOW_PARAM_MULTIPLE_CONNECTIONS;
import static org.jbpm.workflow.instance.rule.RuleType.DRL_LANG;

/**
 * Default implementation of a RuleSet node.
 */
public class RuleSetNode extends StateBasedNode implements ContextContainer {

    private static final long serialVersionUID = 510l;

    private String language = DRL_LANG;

    // NOTE: ContextInstances are not persisted as current functionality (exception scope) does not require it
    private ContextContainer contextContainer = new ContextContainerImpl();

    private RuleType ruleType;

    private Map<String, Object> parameters = new HashMap<>();

    private Supplier<DecisionModel> decisionModel;
    private Supplier<KieRuntime> kieRuntime;
    private RuleUnitFactory<RuleUnitData> ruleUnitFactory;

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Supplier<DecisionModel> getDecisionModel() {
        return decisionModel;
    }

    public void setDecisionModel(Supplier<DecisionModel> decisionModel) {
        this.decisionModel = decisionModel;
    }

    public Supplier<KieRuntime> getKieRuntime() {
        return kieRuntime;
    }

    public RuleUnitFactory<RuleUnitData> getRuleUnitFactory() {
        return ruleUnitFactory;
    }

    public void setRuleUnitFactory(RuleUnitFactory<?> ruleUnitFactory) {
        this.ruleUnitFactory = (RuleUnitFactory<RuleUnitData>) ruleUnitFactory;
    }

    public void setKieRuntime(Supplier<KieRuntime> kieRuntime) {
        this.kieRuntime = kieRuntime;
    }

    @Override
    public void validateAddIncomingConnection(final String type, final Connection connection) {
        super.validateAddIncomingConnection(type, connection);
        if (!Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
            throw new IllegalArgumentException(
                    "This type of node [" + connection.getTo().getUniqueId() + ", " + connection.getTo().getName()
                            + "] only accepts default incoming connection type!");
        }
        if (getFrom() != null && !WORKFLOW_PARAM_MULTIPLE_CONNECTIONS.get(getProcess())) {
            throw new IllegalArgumentException(
                    "This type of node [" + connection.getTo().getUniqueId() + ", " + connection.getTo().getName()
                            + "] cannot have more than one incoming connection!");
        }
    }

    @Override
    public void validateAddOutgoingConnection(final String type, final Connection connection) {
        super.validateAddOutgoingConnection(type, connection);
        if (!Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
            throw new IllegalArgumentException(
                    "This type of node [" + connection.getFrom().getUniqueId() + ", " + connection.getFrom().getName()
                            + "] only accepts default outgoing connection type!");
        }
        if (getTo() != null && !WORKFLOW_PARAM_MULTIPLE_CONNECTIONS.get(getProcess())) {
            throw new IllegalArgumentException(
                    "This type of node [" + connection.getFrom().getUniqueId() + ", " + connection.getFrom().getName()
                            + "] cannot have more than one outgoing connection!");
        }
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setParameter(String param, Object value) {
        this.parameters.put(param, value);
    }

    public Object getParameter(String param) {
        return this.parameters.get(param);
    }

    public Object removeParameter(String param) {
        return this.parameters.remove(param);
    }

    public List<Context> getContexts(String contextType) {
        return contextContainer.getContexts(contextType);
    }

    public void addContext(Context context) {
        ((AbstractContext) context).setContextContainer(this);
        contextContainer.addContext(context);
    }

    public Context getContext(String contextType, long id) {
        return contextContainer.getContext(contextType, id);
    }

    public void setDefaultContext(Context context) {
        ((AbstractContext) context).setContextContainer(this);
        contextContainer.setDefaultContext(context);
    }

    public Context getDefaultContext(String contextType) {
        return contextContainer.getDefaultContext(contextType);
    }

    @Override
    public Context getContext(String contextId) {
        Context context = getDefaultContext(contextId);
        if (context != null) {
            return context;
        }
        return super.getContext(contextId);
    }

}
