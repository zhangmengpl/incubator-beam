/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.beam.runners.direct;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import org.apache.beam.runners.direct.CommittedResult.OutputType;
import org.apache.beam.runners.direct.DirectRunner.UncommittedBundle;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.AppliedPTransform;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link StepTransformResult}.
 */
@RunWith(JUnit4.class)
public class StepTransformResultTest {
  private AppliedPTransform<?, ?, ?> transform;
  private BundleFactory bundleFactory;
  private PCollection<Integer> pc;

  @Before
  public void setup() {
    TestPipeline p = TestPipeline.create();
    pc = p.apply(Create.of(1, 2, 3));
    transform = pc.getProducingTransformInternal();

    bundleFactory = ImmutableListBundleFactory.create();
  }

  @Test
  public void producedBundlesProducedOutputs() {
    UncommittedBundle<Integer> bundle = bundleFactory.createBundle(pc);
    TransformResult result = StepTransformResult.withoutHold(transform).addOutput(bundle)
        .build();

    assertThat(result.getOutputBundles(), Matchers.<UncommittedBundle>containsInAnyOrder(bundle));
  }

  @Test
  public void withAdditionalOutputProducedOutputs() {
    TransformResult result = StepTransformResult.withoutHold(transform)
        .withAdditionalOutput(OutputType.PCOLLECTION_VIEW)
        .build();

    assertThat(result.getOutputTypes(), containsInAnyOrder(OutputType.PCOLLECTION_VIEW));
  }

  @Test
  public void producedBundlesAndAdditionalOutputProducedOutputs() {
    TransformResult result = StepTransformResult.withoutHold(transform)
        .addOutput(bundleFactory.createBundle(pc))
        .withAdditionalOutput(OutputType.PCOLLECTION_VIEW)
        .build();

    assertThat(result.getOutputTypes(), hasItem(OutputType.PCOLLECTION_VIEW));
  }

  @Test
  public void noBundlesNoAdditionalOutputProducedOutputsFalse() {
    TransformResult result = StepTransformResult.withoutHold(transform).build();

    assertThat(result.getOutputTypes(), emptyIterable());
  }
}
