package spiralcraft.textgen.compiler;

import java.util.List;

import spiralcraft.textgen.Element;

class DefineElement
  extends Element
{
  
  private final DefineUnit unit;
  private final List<TglUnit> overlay;
  
  public DefineElement
    (DefineUnit unit,List<TglUnit> overlay)
  { 
    this.setCodePosition(unit.getPosition());
    this.unit=unit;
    this.overlay=overlay;
  }
  

  
  public List<TglUnit> getOverlay()
  { return overlay;
  }
  
  public boolean isFromUnit(DefineUnit unit)
  { return this.unit==unit;
  }

}