package io.dogecube.bouncycastle;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

/**
 * A simple version of ECPoint that has a public constructor and can be created without performing expensive computation/validation.
 * Should be used only for vanity address generation.
 */
public class ECPointSimple extends ECPoint.AbstractFp {

    public ECPointSimple(ECCurve curve, ECFieldElement x, ECFieldElement y) {
        super(curve, x, y);
    }

    protected ECPoint detach() {
        throw new RuntimeException("detach() is not supported!");
    }

    public ECPoint add(ECPoint b) {
        throw new RuntimeException("add() is not supported!");
    }

    public ECPoint twice() {
        throw new RuntimeException("twice() is not supported!");
    }

    public ECPoint negate() {
        throw new RuntimeException("twice() is not supported!");
    }
}
